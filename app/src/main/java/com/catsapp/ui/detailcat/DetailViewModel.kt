package com.catsapp.ui.detailcat

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.catsapp.model.Cat
import com.catsapp.model.repository.CatsRepository
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "DetailViewModel"

class DetailViewModel(private val repository: CatsRepository) : ViewModel() {

    val networkStatus: StateFlow<Boolean> =
        NetworkConnectivityProvider.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            true
        )

    private val _catState = MutableStateFlow<Cat?>(null)
    val catState: StateFlow<Cat?> = _catState

    private val _messageToDisplay = MutableStateFlow("")
    val messageToDisplay: StateFlow<String> = _messageToDisplay

    init {
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
                Log.d(TAG, "cat updated = $cat")
                if (cat == null) return@collect

                _catState.value = cat
            }
        }
    }

    fun getCatById(id: String) {
        viewModelScope.launch {
            _catState.value = repository.getCat(id)
        }
    }

    fun addCatToFavourite(cat: Cat) {
        viewModelScope.launch {
            Log.d(TAG, "addCatToFavourite | cat=$cat")
            val response = repository.addCatToFavorite(cat) ?: run {
                Log.d(TAG, "addCatToFavourite | failed network request")
                setDisplayMessage("[ERROR] ${cat.breedName} was not added to favourites. Try again later!")
                return@launch
            }

            repository.updateCat(cat.copy(favouriteId = response.id, isFavourite = true))
            setDisplayMessage("${cat.breedName} was added to favourites!")
        }
    }

    fun removeCatFromFavorite(cat: Cat) {
        viewModelScope.launch {
            Log.d(TAG, "removeCatFromFavorite | cat=$cat")
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

    companion object {

        /**
         * Factory for creating instances of [DetailViewModel].
         */
        val Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                DetailViewModel(CatsRepository.getInstance(application.applicationContext))
            }
        }
    }
}