package com.kapkabi.helpmeneger.ui.navigation

/**
 * Every screen's route lives here as a sealed class member, so adding a new screen never
 * requires touching an existing route definition.
 */
sealed class Routes(val route: String) {
    data object RecipeList : Routes("recipe_list")
    data object RecipeDetail : Routes("recipe_detail/{recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"
        fun createRoute(recipeId: String) = "recipe_detail/$recipeId"
    }
    data object RecipeFormNew : Routes("recipe_form")
    data object RecipeFormEdit : Routes("recipe_form?recipeId={recipeId}") {
        const val ARG_RECIPE_ID = "recipeId"
        fun createRoute(recipeId: String) = "recipe_form?recipeId=$recipeId"
    }
}
