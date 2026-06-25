package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kapkabi.helpmeneger.data.local.entity.RecipeIngredientEntity
import kotlinx.coroutines.flow.Flow

data class RecipeIngredientRow(
    val id: String,
    val recipeId: String,
    val ingredientId: Long,
    val name: String,
    val quantity: Double,
    val unit: String,
    val sortOrder: Int,
)

@Dao
interface RecipeIngredientDao {
    @Query(
        """
        SELECT ri.id as id, ri.recipe_id as recipeId, ri.ingredient_id as ingredientId,
               i.name as name, ri.quantity as quantity, ri.unit as unit,
               ri.sort_order as sortOrder
        FROM recipe_ingredients ri
        JOIN ingredients i ON i.id = ri.ingredient_id
        WHERE ri.recipe_id = :recipeId
        ORDER BY ri.sort_order
        """
    )
    fun observeForRecipe(recipeId: String): Flow<List<RecipeIngredientRow>>

    @Query("DELETE FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteForRecipe(recipeId: String)

    @Insert
    suspend fun insertAll(ingredients: List<RecipeIngredientEntity>)
}
