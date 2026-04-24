package com.afquintana.buycheaper.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Login : NavRoutes("login")
    data object List : NavRoutes("shopping_list")
    data object AddProduct : NavRoutes("add_product")
    data object AddSection : NavRoutes("add_section")
    data object AddSupermarket : NavRoutes("add_supermarket")
    data object ProductDetail : NavRoutes("product_detail/{productId}") {
        fun create(productId: String) = "product_detail/$productId"
    }
}
