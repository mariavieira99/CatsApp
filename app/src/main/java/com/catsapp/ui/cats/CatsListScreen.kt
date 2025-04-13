package com.catsapp.ui.cats

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.catsapp.model.Cat
import com.catsapp.ui.theme.PurpleGrey80
import com.swordhealth.catsapp.R

@Composable
fun CatsListScreen(
    innerPadding: PaddingValues,
    viewModel: CatsListViewModel,
    gridState: LazyGridState,
    navigationCallback: (String) -> Unit
) {
    val context = LocalContext.current
    val cats = viewModel.catsState.collectAsState().value

    val messageToDisplay = viewModel.messageToDisplay.collectAsState().value
    if (messageToDisplay.isNotEmpty()) {
        Toast.makeText(context, messageToDisplay, Toast.LENGTH_SHORT).show()
        viewModel.setDisplayMessage()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(color = PurpleGrey80)
    ) {
        Text(
            text = "Search Bar Placeholder",
            modifier = Modifier
                .padding(top = 50.dp)
                .align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.padding(innerPadding),
        ) {
            items(cats) { cat ->
                CatBreed(cat = cat, navigationCallback = navigationCallback) {
                    when {
                        !viewModel.networkStatus.value -> Toast.makeText(
                            context,
                            "No internet connection, try again later!",
                            Toast.LENGTH_SHORT
                        ).show()

                        cat.isFavourite -> viewModel.removeCatFromFavorite(cat)

                        else -> viewModel.addCatToFavourite(cat)
                    }
                }
            }
        }
    }
}

@Composable
fun CatBreed(
    cat: Cat,
    navigationCallback: (String) -> Unit,
    favouriteClickCallback: (Unit) -> Unit
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                navigationCallback(cat.id)
            })
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = cat.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(1f)
                )

                Icon(
                    painter = if (cat.isFavourite) painterResource(R.drawable.baseline_star_24)
                    else painterResource(R.drawable.baseline_star_outline_24),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(30.dp)
                        .shadow(
                            20.dp,
                            shape = CircleShape,
                            clip = true,
                            spotColor = Color.White,
                        )
                        .clickable {
                            favouriteClickCallback.invoke(Unit)
                        }
                )
            }

            Text(
                text = cat.breedName,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

}