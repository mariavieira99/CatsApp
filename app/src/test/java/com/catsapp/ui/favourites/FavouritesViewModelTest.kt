package com.catsapp.ui.favourites

import android.app.Application
import android.util.Log
import com.catsapp.model.Cat
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FavouritesViewModel
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

    private val catsList = mutableListOf(cat1, cat3)

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
    fun `init getCatsFromDb success should update favouritesCatsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getFavouriteCatsFromDb() } answers { catsList }

        // Act
        viewModel = FavouritesViewModel(mockkRepository)
        advanceUntilIdle()

        // Assert
        assertEquals(catsList, viewModel.favouritesCatsState.value)
        coVerify { mockkRepository.getFavouriteCatsFromDb() }
    }

    @Test
    fun `removeCatFromFavorite success should update favouritesCatsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getFavouriteCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { true }
        val updatedCat1 = cat1.copy(favouriteId = -1, isFavourite = false)
        coEvery { mockkRepository.updateCat(updatedCat1) } just Runs
        catFlow.value = updatedCat1
        viewModel = FavouritesViewModel(mockkRepository)

        // Act
        viewModel.removeCatFromFavorite(cat1)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.favouritesCatsState.value.find { it.id == cat1.id } == null)
        coVerify { mockkRepository.getFavouriteCatsFromDb() }
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
        coVerify { mockkRepository.updateCat(updatedCat1) }
    }

    @Test
    fun `removeCatFromFavorite error should not update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.getFavouriteCatsFromDb() } answers { catsList }
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { false }
        viewModel = FavouritesViewModel(mockkRepository)

        // Act
        viewModel.removeCatFromFavorite(cat1)
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.favouritesCatsState.value.find { it.id == cat1.id } != null)
        coVerify { mockkRepository.getFavouriteCatsFromDb() }
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
    }

    @Test
    fun `calculateAverageLifespan should return correct average`() = runTest {
        // Arrange
        coEvery { mockkRepository.getFavouriteCatsFromDb() } answers { catsList }
        viewModel = FavouritesViewModel(mockkRepository)
        advanceUntilIdle()

        // Act
        val averageLifespan = viewModel.calculateAverage()

        // Assert
        assertEquals("13.5", averageLifespan)
    }

}