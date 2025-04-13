package com.catsapp

/**
 * App screens
 * @param route the route defined on NavHost to each screen
 */
enum class Screen(val route: String) {
    CATS_LIST("cats_list"),
    FAVOURITES_LIST("favourites_list")
}