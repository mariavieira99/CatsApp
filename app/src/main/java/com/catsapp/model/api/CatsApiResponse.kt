package com.catsapp.model.api

import com.google.gson.annotations.SerializedName

data class CatResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val breedName: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("temperament") val temperament: String,
    @SerializedName("description") val description: String,
    @SerializedName("image") val image: ImageResponse?,
)

data class ImageResponse(
    @SerializedName("id") val id: String,
    @SerializedName("url") val url: String,
)

data class FavouriteCatResponse(
    @SerializedName("image_id") val imageId: String,
)