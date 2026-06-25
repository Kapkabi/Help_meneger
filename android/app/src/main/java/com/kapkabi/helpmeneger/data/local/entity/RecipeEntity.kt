package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mirrors the desktop `recipes` table across migration_0001 (initial) and migration_0003
 * (is_synced). `id` is a TEXT UUID generated on-device via UUID.randomUUID() — never an
 * autoincrement integer — so two devices can create recipes offline without ID collisions
 * once sync ships.
 */
@Entity(
    tableName = "recipes",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["owner_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("category_id"), Index("title"), Index("owner_id")],
)
data class RecipeEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val steps: String = "",
    @ColumnInfo(name = "cook_time_minutes")
    val cookTimeMinutes: Int = 0,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null,
    @ColumnInfo(name = "owner_id")
    val ownerId: Long? = null,
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false,
)
