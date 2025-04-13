package com.catsapp.ui.cats

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.catsapp.model.Cat
import com.catsapp.model.mapToCatModel
import com.catsapp.model.repository.CatsRepository
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "CatsListViewModel"

class CatsListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatsRepository = CatsRepository.getInstance(application)

    val networkStatus: StateFlow<Boolean> =
        NetworkConnectivityProvider.isConnected.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            true
        )

    private val _catsState = MutableStateFlow<List<Cat>>(emptyList())
    val catsState: StateFlow<List<Cat>> = _catsState

    private val _messageToDisplay = MutableStateFlow("")
    val messageToDisplay: StateFlow<String> = _messageToDisplay

    init {
        viewModelScope.launch {
            val cats = loadCatsData()
            if (cats != null) _catsState.value = cats
        }

        viewModelScope.launch {
            networkStatus
                .drop(1)
                .collect { status ->
                    Log.d(TAG, "network status=$status")
                    if (status) {
                        val catsApi = loadCatsDataFromApi()
                        if (catsApi != null) _catsState.value = catsApi
                    }
                }
        }

        viewModelScope.launch {
            repository.catUpdated.collect { cat ->
                Log.d(TAG, "cat updated = $cat")
                if (cat == null) return@collect

                _catsState.value = _catsState.value.map { if (it.id == cat.id) cat else it }
            }
        }
    }

    private suspend fun loadCatsData(): List<Cat>? {
        Log.d(TAG, "loadCatsData | try to fetch from database first")
        val catsFromDb = repository.getCatsFromDb()
        if (catsFromDb.isEmpty()) {
            Log.d(TAG, "loadCatsData | database is empty, fetching from network")
            return loadCatsDataFromApi()
        }

        return catsFromDb
    }

    private suspend fun loadCatsDataFromApi(): List<Cat>? {
        val cats = repository.fetchCatsFromApi() ?: return null
        if (cats.isNotEmpty()) repository.saveCatsToDb(cats.map { it.mapToCatModel() })
        return cats
    }

    fun addCatToFavourite(cat: Cat) {
        viewModelScope.launch {
            Log.d(TAG, "addCatToFavourite | cat=$cat")
            val response = repository.addCatToFavorite(cat) ?: run {
                Log.d(TAG, "addCatToFavourite | network request failed")
                setDisplayMessage("[ERROR] ${cat.breedName} was not added to favourites. Try again later!")
                return@launch
            }

            repository.updateCat(cat.copy(favouriteId = response.id, isFavourite = true))
            setDisplayMessage("${cat.breedName} added to favourites!")
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
}