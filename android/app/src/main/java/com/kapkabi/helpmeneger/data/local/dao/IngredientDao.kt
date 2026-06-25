package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kapkabi.helpmeneger.data.local.entity.IngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients")
    suspend fun getAllOnce(): List<IngredientEntity>

    @Insert
    suspend fun insert(ingredient: IngredientEntity): Long

    /**
     * SQLite's COLLATE NOCASE / LOWER() only fold ASCII case, so matching is done in Kotlin
     * (locale-aware) instead — same approach as the desktop repository's casefold comparison.
     */
    suspend fun getOrCreate(name: String): IngredientEntity {
        getAllOnce().firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        val id = insert(IngredientEntity(name = name))
        return IngredientEntity(id = id, name = name)
    }
}
