package com.catsapp.ui.cats

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.catsapp.model.db.CatModel
import com.catsapp.model.db.CatsDatabase
import com.catsapp.model.repository.CatsRepository
import com.swordhealth.catsapp.R
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CatsListScreenTest {

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

    private var cat2 = CatModel(
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

    private val catsList = listOf(cat1, cat2)
    private lateinit var repository: CatsRepository
    private lateinit var viewModel: CatsListViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val database = Room.inMemoryDatabaseBuilder(
            context,
            CatsDatabase::class.java
        ).allowMainThreadQueries().build()

        val dao = database.catsDao()

        runBlocking {
            dao.insertCats(catsList)
        }

        repository = CatsRepository(dao)
        viewModel = CatsListViewModel(repository)
    }

    @Test
    fun catsList_displayItems() {
        rule.setContent {
            CatsListScreen(
                innerPadding = PaddingValues(1.dp),
                viewModel = viewModel,
                gridState = rememberLazyGridState(),
                query = "",
                queryChangeCallback = {},
                navigationCallback = {},
            )
        }

        rule.onNodeWithText(cat1.breedName).assertIsDisplayed()
        rule.onNodeWithText(cat2.breedName).assertIsDisplayed()
    }

    @Test
    fun catsList_queryItemsAndShowOnly1() {
        rule.setContent {
            var query by remember { mutableStateOf("") }
            CatsListScreen(
                innerPadding = PaddingValues(1.dp),
                viewModel = viewModel,
                gridState = rememberLazyGridState(),
                query = query,
                queryChangeCallback = { query = it },
                navigationCallback = {},
            )
        }

        val textFieldNode =
            rule.onNodeWithText(rule.activity.getString(R.string.search_placeholder))
        textFieldNode.performTextInput(cat1.breedName)

        rule.onNodeWithContentDescription(cat1.breedName).assertIsDisplayed()
        rule.onNodeWithText(cat2.breedName).assertIsNotDisplayed()
    }
}