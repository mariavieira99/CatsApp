package com.catsapp.model.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface to perform network requests to CatsAPI - [https://thecatapi.com/]
 */
interface CatsApi {
    @GET("breeds")
    suspend fun getCats(): List<CatResponse>

    @GET("favourites")
    suspend fun getFavouriteCats(): List<FavouriteCatResponse>

    @POST("favourites")
    suspend fun addCatToFavourite(@Body favouriteRequest: AddFavouriteCatRequest): AddFavouriteCatResponse

    @DELETE("favourites/{favourite_id}")
    suspend fun removeCatFromFavourite(@Path("favourite_id") id: Int): RemoveFavouriteCatResponse
}