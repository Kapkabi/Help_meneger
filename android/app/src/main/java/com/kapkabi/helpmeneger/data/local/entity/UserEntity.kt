package com.kapkabi.helpmeneger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Mirrors the desktop `users` table. Not surfaced in UI yet — reserved for multi-user/sync. */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
)
