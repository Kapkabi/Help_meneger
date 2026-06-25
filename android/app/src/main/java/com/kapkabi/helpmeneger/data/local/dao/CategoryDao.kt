package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kapkabi.helpmeneger.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY is_builtin DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getAllOnce(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: Long): Flow<CategoryEntity?>

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    /** See IngredientDao.getOrCreate for why this matches in Kotlin, not SQL COLLATE NOCASE. */
    suspend fun getOrCreate(name: String): CategoryEntity {
        getAllOnce().firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        val id = insert(CategoryEntity(name = name, isBuiltin = false))
        return CategoryEntity(id = id, name = name, isBuiltin = false)
    }
}
