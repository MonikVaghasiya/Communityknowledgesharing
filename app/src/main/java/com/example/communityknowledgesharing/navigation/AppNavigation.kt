package com.example.communityknowledgesharing.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.communityknowledgesharing.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }

        composable("home") {
            MainScreenLayout(navController, currentRoute = "home", topBarTitle = "Community Feed") {
                HomeScreen(navController)
            }
        }

        composable("profile") {
            MainScreenLayout(navController, currentRoute = "profile", topBarTitle = "My Profile") {
                ProfileScreen(navController)
            }
        }

        composable("connections") {
            MainScreenLayout(navController, currentRoute = "connections", topBarTitle = "Connections") {
                ConnectionsScreen(navController)
            }
        }

        composable("upload") { UploadPostScreen(navController) }

        composable(
            route = "postDetail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            if (postId != null) {
                PostDetailScreen(navController = navController, postId = postId)
            }
        }

        composable(
            route = "publicProfile/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            if (username != null) {
                PublicProfileScreen(username = username, navController = navController)
            }
        }
    }
}
