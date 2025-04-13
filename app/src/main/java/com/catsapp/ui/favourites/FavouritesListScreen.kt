package com.catsapp.ui.favourites

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catsapp.ui.cats.CatBreed

@Composable
fun FavouritesListScreen(innerPadding: PaddingValues) {
    val viewModel: FavouritesViewModel = viewModel()
    val cats = viewModel.favouritesCatsState.collectAsState().value

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        items(cats) { cat ->
            CatBreed(cat) {
                viewModel.removeCatFromFavorite(cat)
            }
        }
    }
}