package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mirrors the desktop `recipe_history` table. Reserved for a future change-history / audit
 * feature — no UI yet. `snapshot_json` holds a serialized copy of the recipe at change time.
 */
@Entity(
    tableName = "recipe_history",
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
            childColumns = ["changed_by"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("recipe_id"), Index("changed_by")],
)
data class RecipeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
    @ColumnInfo(name = "changed_by")
    val changedBy: Long? = null,
    @ColumnInfo(name = "change_type")
    val changeType: String,
    @ColumnInfo(name = "snapshot_json")
    val snapshotJson: String,
    @ColumnInfo(name = "changed_at")
    val changedAt: String,
)
