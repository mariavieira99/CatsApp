package com.catsapp.model.repository

import android.content.Context
import android.util.Log
import com.catsapp.model.Cat
import com.catsapp.model.api.AddFavouriteCatResponse
import com.catsapp.model.api.CatsWebService
import com.catsapp.model.db.CatModel
import com.catsapp.model.db.CatsDao
import com.catsapp.model.db.CatsDatabase
import com.catsapp.model.mapToCat
import com.catsapp.model.mapToCatModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

private const val TAG = "CatsRepository"

class CatsRepository(
    private val dao: CatsDao,
    private val webService: CatsWebService = CatsWebService()
) {

    // region API

    suspend fun fetchCatsFromApi(): List<Cat> = withContext(Dispatchers.IO) {
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
        }
    }

    suspend fun addCatToFavorite(cat: Cat): AddFavouriteCatResponse? {
        val response = webService.addFavouriteCat(cat.imageId) ?: return null
        Log.d("addCatToFavorite", "response=$response")
        return response
    }

    suspend fun removeCatFromFavourite(cat: Cat): Boolean {
        val response = webService.removeCatFromFavourite(cat.favouriteId) ?: return false
        Log.d("removeCatFromFavourite", "response=$response")
        return response.message == "SUCCESS"
    }

    // endregion

    // region DB

    fun getCatsFromDb(): List<Cat> {
        val cats = dao.getAllCats()
        return cats.map { it.mapToCat() }
    }

    fun getFavouriteCatsFromDb(): List<Cat> {
        val favouriteCats = dao.getFavouriteCats()
        return favouriteCats.map { it.mapToCat() }
    }

    suspend fun saveCatsToDb(cats: List<CatModel>) {
        dao.deleteAllCats()
        dao.insertCats(cats)
    }

    suspend fun updateUser(cat: Cat) {
        Log.d(TAG, "updateUser | cat=$cat")
        dao.updateCat(cat.mapToCatModel())
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