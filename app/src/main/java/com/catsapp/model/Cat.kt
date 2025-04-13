package com.catsapp.model

import com.catsapp.model.api.CatResponse
import com.catsapp.model.db.CatModel

data class Cat(
    val id: String,
    val breedName: String,
    val imageId: String,
    val imageUrl: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val favouriteId: Int,
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
    favouriteId = -1,
    isFavourite = false,
)

/**
 * Map DB response to [Cat] data class
 */
fun CatModel.mapToCat() = Cat(
    id = id,
    breedName = breedName,
    imageId = imageId,
    imageUrl = imageUrl,
    origin = origin,
    temperament = temperament,
    description = description,
    favouriteId = favouriteId,
    isFavourite = isFavourite,
)

/**
 * Map DB response to [Cat] data class
 */
fun Cat.mapToCatModel() = CatModel(
    id = id,
    breedName = breedName,
    imageId = imageId,
    imageUrl = imageUrl,
    origin = origin,
    temperament = temperament,
    description = description,
    favouriteId = favouriteId,
    isFavourite = isFavourite,
)