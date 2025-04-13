package com.catsapp.ui.favourites

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catsapp.model.Cat
import com.catsapp.model.mapToCatModel
import com.catsapp.model.repository.CatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "FavouritesViewModel"

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatsRepository = CatsRepository.getInstance(application)

    private val _favouritesCatsState = MutableStateFlow<List<Cat>>(emptyList())
    val favouritesCatsState: StateFlow<List<Cat>> = _favouritesCatsState

    init {
        viewModelScope.launch(Dispatchers.Default) {
            _favouritesCatsState.value = loadCatsData()
        }
    }

    private suspend fun loadCatsData(): List<Cat> {
        Log.d(TAG, "loadCatsData | try to fetch from database first")
        val favouriteCatsFromDb = repository.getFavouriteCatsFromDb()
        if (favouriteCatsFromDb.isEmpty()) {
            Log.d(TAG, "loadCatsData | database is empty, fetching from network")
            val cats = repository.fetchCatsFromApi()
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
            }
        }
    }
}