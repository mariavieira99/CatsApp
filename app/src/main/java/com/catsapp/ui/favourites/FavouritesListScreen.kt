package com.catsapp.ui.favourites

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.catsapp.model.Cat
import com.catsapp.ui.cats.CatBreed

@Composable
fun FavouritesListScreen(innerPadding: PaddingValues) {
    val viewModel: FavouritesViewModel = viewModel()
    val context = LocalContext.current
    val cats = viewModel.favouritesCatsState.collectAsState().value
    val messageToDisplay = viewModel.messageToDisplay.collectAsState().value
    val average = calculateAverage(cats)

    if (messageToDisplay.isNotEmpty()) Toast.makeText(context, messageToDisplay, Toast.LENGTH_SHORT)
        .show()

    Column(Modifier.fillMaxSize()) {
        if (average != null) {
            Text(
                text = "Average higher-lifespan: $average years",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(cats) { cat ->
                CatBreed(cat) {
                    when {
                        !viewModel.networkStatus.value -> Toast.makeText(
                            context,
                            "No internet connection, try again later!",
                            Toast.LENGTH_SHORT
                        ).show()

                        else -> viewModel.removeCatFromFavorite(cat)
                    }
                }
            }
        }
    }
}

private fun calculateAverage(cats: List<Cat>): String? {
    val catsLifeSpan = cats.mapNotNull { cat -> cat.higherLifespan.takeIf { it != -1 && it != 0 } }
    if (catsLifeSpan.isEmpty()) return null

    val average = catsLifeSpan.average()
    return "%.1f".format(average)
}