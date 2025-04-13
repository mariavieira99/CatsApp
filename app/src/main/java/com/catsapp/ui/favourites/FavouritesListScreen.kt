package com.catsapp.ui.favourites

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.catsapp.model.Cat
import com.catsapp.ui.cats.CatBreed
import com.catsapp.ui.theme.PurpleGrey80

@Composable
fun FavouritesListScreen(
    innerPadding: PaddingValues,
    viewModel: FavouritesViewModel,
    gridState: LazyGridState,
    navigationCallback: (String) -> Unit
) {
    val context = LocalContext.current
    val cats = viewModel.favouritesCatsState.collectAsState().value
    val messageToDisplay = viewModel.messageToDisplay.collectAsState().value
    val average = calculateAverage(cats)

    if (messageToDisplay.isNotEmpty()) {
        Toast.makeText(context, messageToDisplay, Toast.LENGTH_SHORT).show()
        viewModel.setDisplayMessage()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(color = PurpleGrey80)
    ) {
        if (cats.isEmpty()) {
            Text(
                text = "No favourite cats. \nGo to Cats List to choose your favourites.",
                modifier = Modifier
                    .padding(top = 80.dp, start = 30.dp, end = 30.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        if (average != null) {
            Text(
                text = "Average higher-lifespan: $average years",
                modifier = Modifier
                    .padding(top = 30.dp)
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.titleLarge
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            items(cats) { cat ->
                CatBreed(cat = cat, navigationCallback = navigationCallback) {
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