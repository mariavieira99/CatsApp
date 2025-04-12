package com.catsapp.model.repository

import com.catsapp.model.Cat
import com.catsapp.model.api.CatsWebService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class CatsRepository(private val webService: CatsWebService = CatsWebService()) {
    suspend fun getCats(): List<Cat> = withContext(Dispatchers.IO) {
        val allCatsDeferred = async { webService.getCats() }
        val favouriteCatsDeferred = async { webService.getFavouriteCats() }

        val allCats = allCatsDeferred.await()
        val favouriteCatsIds = favouriteCatsDeferred.await().map { it.imageId }.toSet()

        val cats = allCats.map { cat ->
            cat.copy(isFavourite = cat.imageId in favouriteCatsIds)
        }

        cats
    }

    companion object {
        @Volatile
        private var instance: CatsRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: CatsRepository().also { instance = it }
        }
    }
}