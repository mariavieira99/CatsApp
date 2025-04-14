package com.catsapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.catsapp.model.Cat
import com.catsapp.ui.cats.CatBreed

val placeholderCat = Cat(
    id = "abys",
    breedName = "Abyssinian",
    imageId = "0XYvRd7oD",
    imageUrl = "https://cdn2.thecatapi.com/images/0XYvRd7oD.jpg",
    origin = "Egypt",
    temperament = "Active, Energetic, Independent, Intelligent, Gentle",
    description = "The Abyssinian is easy to care for, and a joy to have in your home. They’re affectionate cats and love both people and other animals.",
    favouriteId = 123,
    isFavourite = true,
    higherLifespan = 15
)

@Preview(device = Devices.PIXEL_7)
@Composable
fun CatBreedPreview() {
    CatBreed(
        cat = placeholderCat,
        navigationCallback = { },
        favouriteClickCallback = { },
    )
}