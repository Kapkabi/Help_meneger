package com.kapkabi.helpmeneger.domain.repository

import com.kapkabi.helpmeneger.domain.model.Recipe
import com.kapkabi.helpmeneger.domain.model.RecipeSortField
import com.kapkabi.helpmeneger.domain.model.RecipeSummary
import com.kapkabi.helpmeneger.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for recipe persistence. [RoomRecipeRepository] is the only
 * implementation today; a future `ApiRecipeRepository` (cloud sync) can implement this same
 * interface so ViewModels and UI never need to change when sync ships.
 */
interface RecipeRepository {
    fun observeRecipes(
        query: String = "",
        categoryId: Long? = null,
        sortField: RecipeSortField = RecipeSortField.TITLE,
        sortOrder: SortOrder = SortOrder.ASC,
    ): Flow<List<RecipeSummary>>

    fun observeRecipe(id: String): Flow<Recipe?>

    suspend fun createRecipe(recipe: Recipe): String

    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(id: String)
}
