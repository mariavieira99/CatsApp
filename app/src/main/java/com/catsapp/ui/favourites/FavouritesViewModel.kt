package com.catsapp.ui.favourites

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catsapp.model.Cat
import com.catsapp.model.repository.CatsRepository
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "FavouritesViewModel"

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CatsRepository = CatsRepository.getInstance(application)

    private val _favouritesCatsState = MutableStateFlow<List<Cat>>(emptyList())
    val favouritesCatsState: StateFlow<List<Cat>> = _favouritesCatsState

    private val _messageToDisplay = MutableStateFlow("")
    val messageToDisplay: StateFlow<String> = _messageToDisplay

    private val _lifespanAverage = MutableStateFlow<String?>(null)
    val lifespanAverage: StateFlow<String?> = _lifespanAverage

    val networkStatus: StateFlow<Boolean> =
        NetworkConnectivityProvider.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _favouritesCatsState.value = loadCatsData()
            _lifespanAverage.value = calculateAverage()
        }
        
        setupCollectors()
    }

    private fun setupCollectors() {
        viewModelScope.launch {
            networkStatus
                .drop(1)
                .collect { status ->
                    Log.d(TAG, "network status=$status")
                }
        }

        viewModelScope.launch {
            repository.catUpdated.collect { cat ->
                Log.d(TAG, "favouritesCatsState= ${favouritesCatsState.value}")
                Log.d(TAG, "cat updated = $cat")
                if (cat == null) return@collect

                val currentFavourites = _favouritesCatsState.value.toMutableList()
                if (!cat.isFavourite) {
                    currentFavourites.removeIf { it.id == cat.id }
                } else {
                    currentFavourites.add(cat)
                }

                _favouritesCatsState.value = currentFavourites
                _lifespanAverage.value = calculateAverage()
            }
        }

        viewModelScope.launch {
            repository.finishCatsLoad.collect {
                Log.d(TAG, "finishApiCatsLoad")
                _favouritesCatsState.value = loadCatsData()
                _lifespanAverage.value = calculateAverage()
            }
        }
    }

    private suspend fun loadCatsData(): List<Cat> {
        Log.d(TAG, "loadCatsData | fetch from database")
        return repository.getFavouriteCatsFromDb()
    }

    private fun calculateAverage(): String? {
        val catsLifeSpan =
            _favouritesCatsState.value.mapNotNull { cat -> cat.higherLifespan.takeIf { it != -1 && it != 0 } }
        if (catsLifeSpan.isEmpty()) return null

        val average = catsLifeSpan.average()
        return "%.1f".format(average)
    }

    fun removeCatFromFavorite(cat: Cat) {
        viewModelScope.launch {
            val success = repository.removeCatFromFavourite(cat)
            if (success) {
                repository.updateCat(cat.copy(favouriteId = -1, isFavourite = false))
                setDisplayMessage("${cat.breedName} removed from favourites!")
            } else {
                setDisplayMessage("[ERROR] ${cat.breedName} was not removed from favourites. Try again later!")
            }
        }
    }

    fun setDisplayMessage(message: String = "") {
        _messageToDisplay.value = message
    }
}