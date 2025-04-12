package com.catsapp.ui.cats

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.catsapp.model.Cat
import com.swordhealth.catsapp.R

@Composable
fun CatsListScreen(innerPadding: PaddingValues) {
    val viewModel: CatsListViewModel = viewModel()
    val cats = viewModel.catsState.value
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        items(cats) { cat ->
            CatBreed(cat)
        }
    }
}

@Composable
fun CatBreed(cat: Cat) {
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
                    .fillMaxSize()
                    .size(100.dp)
            )

            Icon(
                painter = if (cat.isFavourite) painterResource(R.drawable.baseline_star_24)
                else painterResource(R.drawable.baseline_star_outline_24),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(38.dp)
                    .clickable {
                        Log.d("CatsListScreen", "favourites clicked")
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