package com.afquintana.buycheaper.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.afquintana.buycheaper.presentation.detail.ProductDetailScreen
import com.afquintana.buycheaper.presentation.list.AddProductRoute
import com.afquintana.buycheaper.presentation.list.AddSectionRoute
import com.afquintana.buycheaper.presentation.list.AddSupermarketRoute
import com.afquintana.buycheaper.presentation.list.ShoppingListRoute
import com.afquintana.buycheaper.presentation.login.LoginScreen

@Composable
fun BuyCheaperNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {
        composable(NavRoutes.Login.route) {
            LoginScreen(onLoggedIn = {
                navController.navigate(NavRoutes.List.route) {
                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                }
            })
        }
        composable(NavRoutes.List.route) {
            ShoppingListRoute(
                onProductClick = { productId ->
                    navController.navigate(NavRoutes.ProductDetail.create(productId))
                },
                onAddProduct = {
                    navController.navigate(NavRoutes.AddProduct.route)
                }
            )
        }
        composable(NavRoutes.AddProduct.route) {
            AddProductRoute(
                onBack = { navController.popBackStack() },
                onAddSection = { navController.navigate(NavRoutes.AddSection.route) },
                onAddSupermarket = { navController.navigate(NavRoutes.AddSupermarket.route) }
            )
        }
        composable(NavRoutes.AddSection.route) {
            AddSectionRoute(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.AddSupermarket.route) {
            AddSupermarketRoute(onBack = { navController.popBackStack() })
        }
        composable(
            route = NavRoutes.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            ProductDetailScreen(
                productId = backStackEntry.arguments?.getString("productId").orEmpty(),
                onSaved = { navController.popBackStack() }
            )
        }
    }
}
