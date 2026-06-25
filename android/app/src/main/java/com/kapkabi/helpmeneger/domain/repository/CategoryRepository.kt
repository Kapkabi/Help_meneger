package com.kapkabi.helpmeneger.domain.repository

import com.kapkabi.helpmeneger.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getOrCreate(name: String): Category
}
