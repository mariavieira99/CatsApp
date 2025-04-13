package com.catsapp.ui.favourites

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catsapp.model.Cat
import com.catsapp.model.mapToCatModel
import com.catsapp.model.repository.CatsRepository
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "FavouritesViewModel"

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatsRepository = CatsRepository.getInstance(application)

    private val _favouritesCatsState = MutableStateFlow<List<Cat>>(emptyList())
    val favouritesCatsState: StateFlow<List<Cat>> = _favouritesCatsState

    private val _messageToDisplay = MutableStateFlow("")
    val messageToDisplay: StateFlow<String> = _messageToDisplay

    val networkStatus: StateFlow<Boolean> =
        NetworkConnectivityProvider.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            false
        )

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val favouriteCats = loadCatsData()
            if (favouriteCats != null) _favouritesCatsState.value = favouriteCats
        }
    }

    private suspend fun loadCatsData(): List<Cat>? {
        Log.d(TAG, "loadCatsData | try to fetch from database first")
        val favouriteCatsFromDb = repository.getFavouriteCatsFromDb()
        if (favouriteCatsFromDb.isEmpty()) {
            Log.d(TAG, "loadCatsData | database is empty, fetching from network")
            val cats = repository.fetchCatsFromApi() ?: return null
            if (cats.isNotEmpty()) repository.saveCatsToDb(cats.map { it.mapToCatModel() })
            return cats.filter { it.isFavourite }
        }

        return favouriteCatsFromDb
    }

    fun removeCatFromFavorite(cat: Cat) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.removeCatFromFavourite(cat)
            if (success) {
                repository.updateUser(cat.copy(favouriteId = -1, isFavourite = false))
                val currentFavourites = _favouritesCatsState.value.toMutableList()
                currentFavourites.remove(cat)
                _favouritesCatsState.value = currentFavourites
                _messageToDisplay.value = "${cat.breedName} removed from favourites!"
            } else {
                _messageToDisplay.value =
                    "[ERROR] ${cat.breedName} was not removed from favourites. Try again later!"
            }
        }
    }
}