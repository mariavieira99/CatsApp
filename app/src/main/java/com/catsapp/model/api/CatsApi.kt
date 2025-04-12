package com.catsapp.model.api

import retrofit2.http.GET

/**
 * Interface to perform network requests to CatsAPI - [https://thecatapi.com/]
 */
interface CatsApi {
    @GET("breeds")
    suspend fun getCats(): List<CatResponse>

    @GET("favourites")
    suspend fun getFavouriteCats(): List<FavouriteCatResponse>
}