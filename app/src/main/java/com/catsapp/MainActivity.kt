package com.catsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.catsapp.ui.cats.CatsListScreen
import com.catsapp.ui.favourites.FavouritesListScreen
import com.catsapp.ui.theme.CatsAppTheme
import com.catsapp.utils.NetworkConnectivityProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkConnectivityProvider.init(this)
        NetworkConnectivityProvider.registerCallback()

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "cats_list") {
            composable(route = "cats_list") { CatsListScreen(innerPadding) }
            composable(route = "favourites_list") { FavouritesListScreen(innerPadding) }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavController) {
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
                    navigationIndex.intValue = index
                    navController.navigate(item.route)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (index == navigationIndex.intValue) Color.Black else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary
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
