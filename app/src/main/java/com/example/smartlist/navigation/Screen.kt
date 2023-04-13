package com.example.smartlist.navigation

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home_screen")
    object PurchasesScreen: Screen("purchases_screen")
    object DishesScreen: Screen("dishes_screen")
    object GraphScreen: Screen("graph_screen")

    fun withArgs(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg->
                append("/$arg")
            }
        }
    }
}