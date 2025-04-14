package com.catsapp.ui.detailcat

import android.app.Application
import android.util.Log
import com.catsapp.model.Cat
import com.catsapp.model.api.AddFavouriteCatResponse
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DetailViewModel
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

    private val addFavouriteCatResponse = AddFavouriteCatResponse("SUCCESS", 123)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        application = mockk(relaxed = true)
        mockkRepository = mockk(relaxed = true)

        coEvery { mockkRepository.catUpdated } returns catFlow
        viewModel = DetailViewModel(mockkRepository)
    }

    @Test
    fun `addCatToFavourite success should update catState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.addCatToFavorite(cat2) } answers { addFavouriteCatResponse }
        val updatedCat2 = cat2.copy(favouriteId = 123, isFavourite = true)
        coEvery { mockkRepository.updateCat(updatedCat2) } just Runs
        catFlow.value = updatedCat2

        // Act
        viewModel.addCatToFavourite(cat2)
        advanceUntilIdle()

        // Assert
        assertEquals(updatedCat2, viewModel.catState.value)
        coVerify { mockkRepository.addCatToFavorite(cat2) }
        coVerify { mockkRepository.updateCat(updatedCat2) }
    }

    @Test
    fun `addCatToFavourite error should not update catState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.addCatToFavorite(cat2) } answers { null }

        // Act
        viewModel.addCatToFavourite(cat2)
        advanceUntilIdle()

        // Assert
        assertEquals(null, viewModel.catState.value)
        coVerify { mockkRepository.addCatToFavorite(cat2) }
    }

    @Test
    fun `removeCatFromFavorite success should update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { true }
        val updatedCat1 = cat1.copy(favouriteId = -1, isFavourite = false)
        coEvery { mockkRepository.updateCat(updatedCat1) } just Runs
        catFlow.value = updatedCat1
        viewModel.removeCatFromFavorite(cat1)

        // Act
        advanceUntilIdle()

        // Assert
        assertEquals(updatedCat1, viewModel.catState.value)
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
        coVerify { mockkRepository.updateCat(updatedCat1) }
    }

    @Test
    fun `removeCatFromFavorite error should not update catsState StateFlow`() = runTest {
        // Arrange
        coEvery { mockkRepository.removeCatFromFavourite(cat1) } answers { false }

        // Act
        viewModel.removeCatFromFavorite(cat1)
        advanceUntilIdle()

        // Assert
        assertEquals(null, viewModel.catState.value)
        coVerify { mockkRepository.removeCatFromFavourite(cat1) }
    }
}