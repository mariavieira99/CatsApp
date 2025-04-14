package com.catsapp.ui.detailcat

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.catsapp.model.Cat
import com.catsapp.ui.theme.Purple40
import com.catsapp.ui.theme.PurpleGrey80
import com.swordhealth.catsapp.R

@Composable
fun DetailCatScreen(
    innerPadding: PaddingValues,
    viewModel: DetailViewModel,
    backClickCallback: () -> (Unit)
) {
    val cat = viewModel.catState.collectAsState().value ?: return
    val context = LocalContext.current

    val messageToDisplay = viewModel.messageToDisplay.collectAsState().value
    if (messageToDisplay.isNotEmpty()) {
        Toast.makeText(context, messageToDisplay, Toast.LENGTH_SHORT).show()
        viewModel.setDisplayMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp)
            .background(color = PurpleGrey80)
            .verticalScroll(rememberScrollState())
    ) {
        BackItem(backClickCallback)
        CatInformation(cat) {
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

@Composable
fun BackItem(backClickCallback: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 50.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_arrow_back_ios_24),
            contentDescription = stringResource(R.string.back_item),
            modifier = Modifier
                .size(25.dp)
                .shadow(20.dp, shape = CircleShape, clip = true, spotColor = Color.Black)
                .clickable {
                    backClickCallback()
                }
        )
    }
}

@Composable
fun CatInformation(cat: Cat, favouriteClickCallback: (() -> Unit)? = null) {
    val (painter, contentDescription) = if (cat.isFavourite) {
        painterResource(R.drawable.baseline_star_24) to stringResource(R.string.remove_from_favourites_content_description)
    } else {
        painterResource(R.drawable.baseline_star_outline_24) to stringResource(R.string.add_to_favourites_content_description)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = cat.breedName,
            modifier = Modifier.align(Alignment.CenterVertically),
            style = MaterialTheme.typography.headlineSmall
        )

        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(30.dp)
                .shadow(
                    20.dp,
                    shape = CircleShape,
                    clip = true,
                    spotColor = Color.White
                )
                .clickable {
                    favouriteClickCallback?.invoke()
                },
            tint = Purple40,
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = CircleShape,
            border = BorderStroke(
                width = 2.dp,
                color = Color.Black,
            )
        ) {
            AsyncImage(
                model = cat.imageUrl,
                contentDescription = stringResource(R.string.cat_image_detail_content_description),
                modifier = Modifier.size(300.dp)
            )
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailItems(title = stringResource(R.string.origin), description = cat.origin)
        DetailItems(title = stringResource(R.string.temperament), description = cat.temperament)
        DetailItems(title = stringResource(R.string.description), description = cat.description)
    }
}

@Composable
fun DetailItems(title: String, description: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(color = Color.DarkGray),
    )
    Card(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(8.dp),
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp, max = 200.dp)
                .verticalScroll(rememberScrollState())
                .alpha(0.8f)
                .padding(8.dp)
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}