package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Mirrors the desktop `ratings` table. Reserved for a future rating feature — no UI yet. */
@Entity(
    tableName = "ratings",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("recipe_id"), Index("user_id")],
)
data class RatingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    @ColumnInfo(name = "user_id")
    val userId: Long? = null,
    val score: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
