package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.kapkabi.helpmeneger.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

data class RecipeListRow(
    val id: String,
    val title: String,
    val cookTimeMinutes: Int,
    val photoPath: String?,
    val createdAt: String,
    val updatedAt: String,
    val categoryId: Long?,
    val categoryName: String?,
    val ingredientName: String?,
)

@Dao
interface RecipeDao {
    /**
     * Returns one row per (recipe, ingredient) pair — text/ingredient matching is done in
     * Kotlin (see RoomRecipeRepository) rather than SQL LIKE, because SQLite's default
     * NOCASE/LIKE only case-folds ASCII and this app's content is mostly Cyrillic.
     */
    @Query(
        """
        SELECT r.id as id, r.title as title, r.cook_time_minutes as cookTimeMinutes,
               r.photo_path as photoPath, r.created_at as createdAt, r.updated_at as updatedAt,
               c.id as categoryId, c.name as categoryName, i.name as ingredientName
        FROM recipes r
        LEFT JOIN categories c ON c.id = r.category_id
        LEFT JOIN recipe_ingredients ri ON ri.recipe_id = r.id
        LEFT JOIN ingredients i ON i.id = ri.ingredient_id
        WHERE r.is_deleted = 0
          AND (:categoryId IS NULL OR r.category_id = :categoryId)
        """
    )
    fun observeRecipeList(categoryId: Long?): Flow<List<RecipeListRow>>

    @Query("SELECT * FROM recipes WHERE id = :id AND is_deleted = 0")
    fun observeById(id: String): Flow<RecipeEntity?>

    @Insert
    suspend fun insert(recipe: RecipeEntity)

    @Update
    suspend fun update(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: String)
}
