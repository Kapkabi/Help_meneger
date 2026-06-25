package com.kapkabi.helpmeneger.data.repository

import com.kapkabi.helpmeneger.data.local.dao.CategoryDao
import com.kapkabi.helpmeneger.data.local.entity.CategoryEntity
import com.kapkabi.helpmeneger.domain.model.Category
import com.kapkabi.helpmeneger.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getOrCreate(name: String): Category =
        categoryDao.getOrCreate(name.trim()).toDomain()
}

private fun CategoryEntity.toDomain() = Category(id = id, name = name, isBuiltin = isBuiltin)
