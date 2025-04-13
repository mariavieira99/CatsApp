package com.catsapp.model.api

import com.google.gson.annotations.SerializedName

data class AddFavouriteCatRequest(
    @SerializedName("image_id") val imageId: String,
)