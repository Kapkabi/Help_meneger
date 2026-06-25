package com.kapkabi.helpmeneger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kapkabi.helpmeneger.ui.recipedetail.RecipeDetailScreen
import com.kapkabi.helpmeneger.ui.recipeform.RecipeFormScreen
import com.kapkabi.helpmeneger.ui.recipelist.RecipeListScreen

@Composable
fun HelpMenegerNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.RecipeList.route) {
        composable(Routes.RecipeList.route) {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Routes.RecipeDetail.createRoute(recipeId))
                },
                onAddRecipeClick = {
                    navController.navigate(Routes.RecipeFormNew.route)
                },
            )
        }

        composable(
            route = Routes.RecipeDetail.route,
            arguments = listOf(navArgument(Routes.RecipeDetail.ARG_RECIPE_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Routes.RecipeDetail.ARG_RECIPE_ID).orEmpty()
            RecipeDetailScreen(
                recipeId = recipeId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.RecipeFormEdit.createRoute(recipeId)) },
                onDeleted = { navController.popBackStack() },
            )
        }

        composable(Routes.RecipeFormNew.route) {
            RecipeFormScreen(
                recipeId = null,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.RecipeFormEdit.route,
            arguments = listOf(
                navArgument(Routes.RecipeFormEdit.ARG_RECIPE_ID) {
                    type = NavType.StringType
                    nullable = true
                },
            ),
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString(Routes.RecipeFormEdit.ARG_RECIPE_ID)
            RecipeFormScreen(
                recipeId = recipeId,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
    }
}
