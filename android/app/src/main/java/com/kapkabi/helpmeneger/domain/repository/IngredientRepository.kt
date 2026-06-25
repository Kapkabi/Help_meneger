package com.kapkabi.helpmeneger.domain.repository

import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeIngredients(): Flow<List<String>>
}
