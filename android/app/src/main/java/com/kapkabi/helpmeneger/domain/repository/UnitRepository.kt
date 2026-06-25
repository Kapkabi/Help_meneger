package com.kapkabi.helpmeneger.domain.repository

import com.kapkabi.helpmeneger.domain.model.MeasurementUnit
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun observeUnits(): Flow<List<MeasurementUnit>>
    suspend fun getOrCreate(name: String): MeasurementUnit
}
