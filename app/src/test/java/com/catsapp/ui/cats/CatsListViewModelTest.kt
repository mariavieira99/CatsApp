package com.catsapp.ui.cats

import android.app.Application
import android.util.Log
import com.catsapp.model.Cat
import com.catsapp.model.api.AddFavouriteCatResponse
import com.catsapp.model.mapToCatModel
import com.catsapp.model.repository.CatsRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatsListViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CatsListViewModel
    private lateinit var mockkRepository: CatsRepository
    private lateinit var application: Application
    private val catFlow = MutableStateFlow<Cat?>(null)

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

    private var cat3 = Cat(
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

    private val catsList = listOf(cat1, cat2, cat3)
    private val addFavouriteCatResponse = AddFavouriteCatResponse("SUCCESS", 123)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        application = mockk(relaxed = true)
        mockkRepository = mockk(relaxed = true)

        coEvery { mockkRepository.catUpdated } returns catFlow
        coEvery { mockkRepository.finishCatsLoad } returns MutableSharedFlow()
    }

    @Test
    fun `init fetchCatsFromApi success should update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } returns emptyList()
        coEvery { mockkRepository.fetchCatsFromApi() } returns catsList
        coEvery { mockkRepository.saveCatsToDb(catsList.map { it.mapToCatModel() }) } just Runs

        // Act
        viewModel = CatsListViewModel(mockkRepository)
        advanceUntilIdle()

        // Assert
        assertEquals(catsList, viewModel.catsState.value)
        coVerify { mockkRepository.getCatsFromDb() }
        coVerify { mockkRepository.fetchCatsFromApi() }
        coVerify { mockkRepository.saveCatsToDb(catsList.map { it.mapToCatModel() }) }
    }

    @Test
    fun `init fetchCatsFromApi request already on progress should not update catsState StateFlow`() =
        runTest {
            // Arrange
            coEvery { mockkRepository.getCatsFromDb() } returns emptyList()
            coEvery { mockkRepository.fetchCatsFromApi() } returns null

            // Act
            viewModel = CatsListViewModel(mockkRepository)
            advanceUntilIdle()

            // Assert
            assertEquals(emptyList<Cat>(), viewModel.catsState.value)
            coVerify { mockkRepository.getCatsFromDb() }
            coVerify { mockkRepository.fetchCatsFromApi() }
        }


    @Test
    fun `init getCatsFromDb success should update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } answers { catsList }

        // Act
        viewModel = CatsListViewModel(mockkRepository)
        advanceUntilIdle()

        // Assert
        assertEquals(catsList, viewModel.catsState.value)
        coVerify { mockkRepository.getCatsFromDb() }
    }

    @Test
    fun `addCatToFavourite success should update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.addCatToFavorite(cat2) } answers { addFavouriteCatResponse }
        val updatedCat2 = cat2.copy(favouriteId = 123, isFavourite = true)
        coEvery { mockkRepository.updateCat(updatedCat2) } just Runs
        catFlow.value = updatedCat2
        viewModel = CatsListViewModel(mockkRepository)

        // Act
        viewModel.addCatToFavourite(cat2)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.catsState.value.find { it.id == cat2.id }?.isFavourite == true)
        coVerify { mockkRepository.getCatsFromDb() }
        coVerify { mockkRepository.addCatToFavorite(cat2) }
        coVerify { mockkRepository.updateCat(updatedCat2) }
    }

    @Test
    fun `addCatToFavourite error should not update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.addCatToFavorite(cat2) } answers { null }
        viewModel = CatsListViewModel(mockkRepository)

        // Act
        viewModel.addCatToFavourite(cat2)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.catsState.value.find { it.id == cat2.id }?.isFavourite == false)
        coVerify { mockkRepository.getCatsFromDb() }
        coVerify { mockkRepository.addCatToFavorite(cat2) }
    }

    @Test
    fun `removeCatFromFavorite success should update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { true }
        val updatedCat1 = cat1.copy(favouriteId = -1, isFavourite = false)
        coEvery { mockkRepository.updateCat(updatedCat1) } just Runs
        catFlow.value = updatedCat1
        viewModel = CatsListViewModel(mockkRepository)

        // Act
        viewModel.removeCatFromFavorite(cat1)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.catsState.value.find { it.id == cat1.id }?.isFavourite == false)
        coVerify { mockkRepository.getCatsFromDb() }
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
        coVerify { mockkRepository.updateCat(updatedCat1) }
    }

    @Test
    fun `removeCatFromFavorite error should not update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { false }
        viewModel = CatsListViewModel(mockkRepository)

        // Act
        viewModel.removeCatFromFavorite(cat1)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.catsState.value.find { it.id == cat1.id }?.isFavourite == true)
        coVerify { mockkRepository.getCatsFromDb() }
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}