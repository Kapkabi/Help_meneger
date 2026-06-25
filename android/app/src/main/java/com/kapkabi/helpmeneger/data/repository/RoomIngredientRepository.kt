package com.kapkabi.helpmeneger.data.repository

import com.kapkabi.helpmeneger.data.local.dao.IngredientDao
import com.kapkabi.helpmeneger.domain.repository.IngredientRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomIngredientRepository @Inject constructor(
    private val ingredientDao: IngredientDao,
) : IngredientRepository {

    override fun observeIngredients(): Flow<List<String>> =
        ingredientDao.observeAll().map { list -> list.map { it.name } }
}
