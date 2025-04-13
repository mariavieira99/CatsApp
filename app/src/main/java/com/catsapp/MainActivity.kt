package com.catsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.catsapp.ui.cats.CatsListScreen
import com.catsapp.ui.cats.CatsListViewModel
import com.catsapp.ui.detailcat.DetailCatScreen
import com.catsapp.ui.detailcat.DetailViewModel
import com.catsapp.ui.favourites.FavouritesListScreen
import com.catsapp.ui.favourites.FavouritesViewModel
import com.catsapp.ui.theme.CatsAppTheme
import com.catsapp.ui.theme.Purple40
import com.catsapp.utils.NetworkConnectivityProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkConnectivityProvider.init(this)
        NetworkConnectivityProvider.registerCallback()
        viewModel.startPeriodicUpdates()

        enableEdgeToEdge()
        setContent {
            CatsAppTheme {
                CatsApp()
            }
        }
    }

    override fun onDestroy() {
        NetworkConnectivityProvider.unregisterCallback()
        super.onDestroy()
    }
}

@Composable
private fun CatsApp() {
    val navController = rememberNavController()
    val catsListViewModel: CatsListViewModel = viewModel()
    val favouritesViewModel: FavouritesViewModel = viewModel()

    val allCatsGridState = rememberLazyGridState()
    val favouriteCatsGridState = rememberLazyGridState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController,
                allCatsGridState,
                favouriteCatsGridState,
            )
        },
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "cats_list") {
            composable(route = "cats_list") {
                CatsListScreen(
                    innerPadding = innerPadding,
                    viewModel = catsListViewModel,
                    gridState = allCatsGridState,
                    query = query,
                    queryChangeCallback = {
                        query = it
                    },
                ) {
                    navController.navigate("cat_details/$it")
                }
            }
            composable(route = "favourites_list") {
                FavouritesListScreen(
                    innerPadding = innerPadding,
                    viewModel = favouritesViewModel,
                    gridState = favouriteCatsGridState,
                ) {
                    navController.navigate("cat_details/$it")
                }
            }

            composable(
                route = "cat_details/{catId}",
                arguments = listOf(navArgument(name = "catId") {
                    type = NavType.StringType
                })
            ) { navBackStack ->
                val getCatId = navBackStack.arguments?.getString("catId") ?: return@composable
                val viewModel: DetailViewModel = viewModel()
                viewModel.getCatById(getCatId)

                DetailCatScreen(innerPadding, viewModel) {
                    navController.navigateUp()
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavController,
    allCatsGridState: LazyGridState,
    favouriteCatsGridState: LazyGridState,
) {

    val coroutineScope = rememberCoroutineScope()
    val navigationIndex = rememberSaveable { mutableIntStateOf(0) }

    val navigationItems = listOf(
        NavigationItem(
            title = "Cats List",
            icon = Icons.Default.Home,
            route = Screen.CATS_LIST.route,
        ),
        NavigationItem(
            title = "Favourites List",
            icon = Icons.Default.Star,
            route = Screen.FAVOURITES_LIST.route,
        ),
    )

    NavigationBar(containerColor = Color.White) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = navigationIndex.intValue == index,
                onClick = {
                    val listState = when {
                        navigationIndex.intValue == 0 && index == 0 -> allCatsGridState
                        navigationIndex.intValue == 1 && index == 1 -> favouriteCatsGridState
                        else -> null
                    }
                    navigationIndex.intValue = index

                    if (listState != null) coroutineScope.launch { listState.animateScrollToItem(0) }
                    else navController.navigate(item.route)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (index == navigationIndex.intValue) Color.Black else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = Purple40,
                )

            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
)
