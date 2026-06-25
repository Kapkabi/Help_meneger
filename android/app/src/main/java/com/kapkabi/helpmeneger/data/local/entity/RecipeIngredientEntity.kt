package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mirrors the desktop `recipe_ingredients` table. `unit` stays a free-text column (not a FK
 * to `units`) for the same reason as desktop: units is an autocomplete dictionary, not a
 * constraint. `id` is a UUID (unlike desktop's autoincrement int) per the task requirement
 * that every row a device can create offline gets a UUID for future sync.
 */
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recipe_id"), Index("ingredient_id")],
)
data class RecipeIngredientEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    @ColumnInfo(name = "ingredient_id")
    val ingredientId: Long,
    val quantity: Double = 0.0,
    val unit: String = "",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
)
