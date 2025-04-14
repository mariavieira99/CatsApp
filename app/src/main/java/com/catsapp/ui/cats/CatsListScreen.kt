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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.catsapp.model.Cat
import com.catsapp.ui.theme.Purple40
import com.catsapp.ui.theme.PurpleGrey80
import com.swordhealth.catsapp.R

@Composable
fun CatsListScreen(
    innerPadding: PaddingValues,
    viewModel: CatsListViewModel,
    gridState: LazyGridState,
    query: String,
    queryChangeCallback: (String) -> Unit,
    navigationCallback: (String) -> Unit,
) {
    val context = LocalContext.current
    val cats = viewModel.catsState.collectAsState().value

    val messageToDisplay = viewModel.messageToDisplay.collectAsState().value
    if (messageToDisplay.isNotEmpty()) {
        Toast.makeText(context, messageToDisplay, Toast.LENGTH_SHORT).show()
        viewModel.setDisplayMessage()
    }

    val filteredItems = cats.filter {
        it.breedName.contains(query, ignoreCase = true)
    }

    val searchIcon =
        if (query.isEmpty()) R.drawable.baseline_search_24 else R.drawable.baseline_clear_24

    Column(
        Modifier
            .fillMaxSize()
            .background(color = PurpleGrey80)
    ) {
        TextField(
            value = query,
            onValueChange = { queryChangeCallback.invoke(it) },
            label = { Text(text = stringResource(id = R.string.search_placeholder)) },
            trailingIcon = {
                Icon(
                    painter = painterResource(searchIcon),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        if (query.isNotEmpty()) queryChangeCallback.invoke("")
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(top = 50.dp)
                .align(Alignment.CenterHorizontally),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.padding(innerPadding),
        ) {
            items(filteredItems) { cat ->
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
    navigationCallback: ((String) -> Unit)? = null,
    favouriteClickCallback: (() -> Unit)? = null,
) {
    val (painter, contentDescription) = if (cat.isFavourite) {
        painterResource(R.drawable.baseline_star_24) to stringResource(R.string.remove_from_favourites_content_description)
    } else {
        painterResource(R.drawable.baseline_star_outline_24) to stringResource(R.string.add_to_favourites_content_description)
    }

    Card(
        elevation = CardDefaults.elevatedCardElevation(10.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                navigationCallback?.invoke(cat.id)
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
                    contentDescription = stringResource(R.string.cat_image_list_content_description),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(1f)
                )

                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
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
                            favouriteClickCallback?.invoke()
                        },
                    tint = Purple40,
                )
            }

            Text(
                text = cat.breedName,
                modifier = Modifier
                    .semantics { this.contentDescription = cat.breedName }
                    .padding(bottom = 16.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

}