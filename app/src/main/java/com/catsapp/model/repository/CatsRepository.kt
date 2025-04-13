package com.catsapp.model.repository

import android.content.Context
import android.util.Log
import androidx.room.concurrent.AtomicBoolean
import com.catsapp.model.Cat
import com.catsapp.model.api.CatsWebService
import com.catsapp.model.db.CatModel
import com.catsapp.model.db.CatsDao
import com.catsapp.model.db.CatsDatabase
import com.catsapp.model.mapToCat
import com.catsapp.model.mapToCatModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

private const val TAG = "CatsRepository"

class CatsRepository(
    private val dao: CatsDao,
    private val webService: CatsWebService = CatsWebService(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private var isRequestInProgress = AtomicBoolean(false)

    private val _catUpdated = MutableStateFlow<Cat?>(null)
    val catUpdated: StateFlow<Cat?> = _catUpdated

    private val _finishCatsLoad = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val finishCatsLoad: SharedFlow<Boolean> = _finishCatsLoad.asSharedFlow()

    // region API

    suspend fun fetchCatsFromApi(): List<Cat>? = withContext(ioDispatcher) {
        if (isRequestInProgress.get()) {
            Log.d(TAG, "Request already in progress!")
            return@withContext null
        }

        isRequestInProgress.set(true)

        try {
            val allCatsDeferred = async { webService.getCats() }
            val favouriteCatsDeferred = async { webService.getFavouriteCats() }

            val allCats = allCatsDeferred.await()
            val favouriteCats = favouriteCatsDeferred.await()

            val cats = allCats.map { cat ->
                val favoriteCat = favouriteCats.find { it.imageId == cat.imageId }
                if (favoriteCat != null) {
                    val updatedCat = cat.copy(favouriteId = favoriteCat.id, isFavourite = true)
                    updatedCat
                } else {
                    cat
                }
            }

            Log.d(TAG, "fetchCatsFromApi | cats=${cats.size}")
            cats
        } catch (e: Exception) {
            Log.d(TAG, "fetchCatsFromApi | caught exception=$e")
            emptyList()
        } finally {
            isRequestInProgress.set(false)
        }
    }

    suspend fun addCatToFavorite(cat: Cat) = withContext(ioDispatcher) {
        val response = webService.addFavouriteCat(cat.imageId) ?: return@withContext null
        Log.d("addCatToFavorite", "response=$response")
        response
    }

    suspend fun removeCatFromFavourite(cat: Cat) = withContext(ioDispatcher) {
        val response =
            webService.removeCatFromFavourite(cat.favouriteId) ?: return@withContext false
        Log.d("removeCatFromFavourite", "response=$response")
        response.message == "SUCCESS"
    }

    // endregion

    // region DB

    suspend fun getCatsFromDb() = withContext(ioDispatcher) {
        val cats = dao.getAllCats()
        cats.map { it.mapToCat() }
    }

    suspend fun getFavouriteCatsFromDb() = withContext(ioDispatcher) {
        val favouriteCats = dao.getFavouriteCats()
        favouriteCats.map { it.mapToCat() }
    }

    suspend fun saveCatsToDb(cats: List<CatModel>, isFromPeriodicUpdate: Boolean = false) =
        withContext(ioDispatcher) {
            dao.deleteAllCats()
            dao.insertCats(cats)
            _finishCatsLoad.emit(isFromPeriodicUpdate)
    }

    suspend fun updateCat(cat: Cat) = withContext(ioDispatcher) {
        Log.d(TAG, "updateCat | cat=$cat")
        dao.updateCat(cat.mapToCatModel())
        _catUpdated.value = cat
    }

    suspend fun getCat(id: String) = withContext(ioDispatcher) {
        val cat = dao.getCatById(id) ?: return@withContext null
        cat.mapToCat()
    }

    // endregion

    companion object {
        @Volatile
        private var instance: CatsRepository? = null

        fun getInstance(context: Context) = instance ?: synchronized(this) {
            val database = CatsDatabase.getDatabase(context)
            CatsRepository(database.catsDao()).also { instance = it }
        }
    }
}