package com.kapkabi.helpmeneger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kapkabi.helpmeneger.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units")
    suspend fun getAllOnce(): List<UnitEntity>

    @Insert
    suspend fun insert(unit: UnitEntity): Long

    /** See IngredientDao.getOrCreate for why this matches in Kotlin, not SQL COLLATE NOCASE. */
    suspend fun getOrCreate(name: String): UnitEntity {
        getAllOnce().firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        val id = insert(UnitEntity(name = name))
        return UnitEntity(id = id, name = name)
    }
}
