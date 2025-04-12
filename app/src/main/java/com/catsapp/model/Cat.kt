package com.catsapp.model

import com.catsapp.model.api.CatResponse

data class Cat(
    val id: String,
    val breedName: String,
    val imageId: String,
    val imageUrl: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val isFavourite: Boolean,
)

/**
 * Map the network response to [Cat] data class
 */
fun CatResponse.mapToCat() = Cat(
    id = id,
    breedName = breedName,
    imageId = image?.id ?: "",
    imageUrl = image?.url ?: "",
    origin = origin,
    temperament = temperament,
    description = description,
    isFavourite = false,
)