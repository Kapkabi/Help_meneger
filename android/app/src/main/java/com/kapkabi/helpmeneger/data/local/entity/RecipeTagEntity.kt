package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Mirrors the desktop `recipe_tags` join table. Reserved for a future tagging feature. */
@Entity(
    tableName = "recipe_tags",
    primaryKeys = ["recipe_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = com.kapkabi.helpmeneger.data.local.entity.RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("tag_id")],
)
data class RecipeTagEntity(
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
)
