package com.catsapp.ui.detailcat

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.catsapp.model.Cat
import com.swordhealth.catsapp.R
import org.junit.Rule
import org.junit.Test

class DetailCatScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var cat1 = Cat(
        id = "cat1",
        breedName = "Cat 1",
        imageId = "123",
        imageUrl = "url",
        origin = "France",
        temperament = "Friendly",
        description = "Some description",
        favouriteId = 123,
        isFavourite = true,
        higherLifespan = 15,
    )

    private var cat2 = Cat(
        id = "cat2",
        breedName = "Cat 2",
        imageId = "1234",
        imageUrl = "url",
        origin = "France",
        temperament = "Friendly",
        description = "Some description",
        favouriteId = -1,
        isFavourite = false,
        higherLifespan = 10,
    )

    private val catImageDetailContentDescription by lazy { rule.activity.getString(R.string.cat_image_detail_content_description) }
    private val addToFavourites by lazy { rule.activity.getString(R.string.add_to_favourites_content_description) }
    private val removeFromFavourites by lazy { rule.activity.getString(R.string.remove_from_favourites_content_description) }
    private val origin by lazy { rule.activity.getString(R.string.origin) }
    private val temperament by lazy { rule.activity.getString(R.string.temperament) }
    private val description by lazy { rule.activity.getString(R.string.description) }

    @Test
    fun showAllCatParameters_withSuccess() {
        with(rule) {
            setContent {
                CatInformation(cat1)
            }
            onNodeWithText(cat1.breedName).assertIsDisplayed()
            onNodeWithContentDescription(catImageDetailContentDescription).assertIsDisplayed()
            onNodeWithContentDescription(removeFromFavourites).assertIsDisplayed()
            onNodeWithText(origin).assertIsDisplayed()
            onNodeWithText(cat1.origin).assertIsDisplayed()
            onNodeWithText(temperament).assertIsDisplayed()
            onNodeWithText(cat1.temperament).assertIsDisplayed()
            onNodeWithText(description).assertIsDisplayed()
            onNodeWithText(cat1.description).assertIsDisplayed()
        }
    }

    @Test
    fun removeCatFromFavourites_withSuccess() {
        rule.setContent {
            var catState by remember { mutableStateOf(cat1) }
            CatInformation(catState, favouriteClickCallback = {
                catState = catState.copy(isFavourite = false)
            })
        }

        rule.onNodeWithContentDescription(removeFromFavourites).performClick()
        rule.onNodeWithContentDescription(addToFavourites).assertIsDisplayed()
    }

    @Test
    fun addCatToFavourites_withSuccess() {
        rule.setContent {
            var catState by remember { mutableStateOf(cat2) }
            CatInformation(catState, favouriteClickCallback = {
                catState = catState.copy(isFavourite = true)
            })
        }

        rule.onNodeWithContentDescription(addToFavourites).performClick()
        rule.onNodeWithContentDescription(removeFromFavourites).assertIsDisplayed()
    }
}