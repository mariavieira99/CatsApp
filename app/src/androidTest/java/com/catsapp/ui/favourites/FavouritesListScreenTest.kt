package com.catsapp.ui.favourites

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.catsapp.model.db.CatModel
import com.catsapp.model.db.CatsDao
import com.catsapp.model.db.CatsDatabase
import com.catsapp.model.repository.CatsRepository
import com.swordhealth.catsapp.R
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavouritesListScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var cat1 = CatModel(
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

    private var cat3 = CatModel(
        id = "cat3",
        breedName = "Cat 3",
        imageId = "12345",
        imageUrl = "url",
        origin = "France",
        temperament = "Friendly",
        description = "Some description",
        favouriteId = 12345,
        isFavourite = true,
        higherLifespan = 12,
    )

    private val favouritesCatsList = listOf(cat1, cat3)
    private lateinit var dao: CatsDao
    private lateinit var repository: CatsRepository
    private lateinit var viewModel: FavouritesViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            CatsDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.catsDao()
        repository = CatsRepository(dao)
    }

    @Test
    fun catsFavouriteList_displayItems() {
        runBlocking {
            dao.insertCats(favouritesCatsList)
        }

        viewModel = FavouritesViewModel(repository)

        rule.setContent {
            FavouritesListScreen(
                innerPadding = PaddingValues(1.dp),
                viewModel = viewModel,
                gridState = rememberLazyGridState(),
                navigationCallback = {},
            )
        }

        rule.onNodeWithText(cat1.breedName).assertIsDisplayed()
        rule.onNodeWithText(cat3.breedName).assertIsDisplayed()
    }

    @Test
    fun catsFavouriteList_displayCorrectAverage() {
        runBlocking {
            dao.insertCats(favouritesCatsList)
        }

        viewModel = FavouritesViewModel(repository)

        rule.setContent {
            FavouritesListScreen(
                innerPadding = PaddingValues(1.dp),
                viewModel = viewModel,
                gridState = rememberLazyGridState(),
                navigationCallback = {},
            )
        }

        val averageLifespan = rule.activity.getString(R.string.average_lifespan, 13.5)
        rule.onNodeWithText(averageLifespan).assertIsDisplayed()
    }

    @Test
    fun removeAllCats_displayMessage() {
        runBlocking {
            dao.deleteAllCats()
        }

        viewModel = FavouritesViewModel(repository)

        rule.setContent {
            FavouritesListScreen(
                innerPadding = PaddingValues(1.dp),
                viewModel = viewModel,
                gridState = rememberLazyGridState(),
                navigationCallback = {},
            )
        }
        rule.onNodeWithText(rule.activity.getString(R.string.no_favourite_cats_message))
            .assertIsDisplayed()
    }
}