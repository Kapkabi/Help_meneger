package com.kapkabi.helpmeneger.data.repository

import com.kapkabi.helpmeneger.data.local.dao.UnitDao
import com.kapkabi.helpmeneger.data.local.entity.UnitEntity
import com.kapkabi.helpmeneger.domain.model.MeasurementUnit
import com.kapkabi.helpmeneger.domain.repository.UnitRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomUnitRepository @Inject constructor(
    private val unitDao: UnitDao,
) : UnitRepository {

    override fun observeUnits(): Flow<List<MeasurementUnit>> =
        unitDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getOrCreate(name: String): MeasurementUnit =
        unitDao.getOrCreate(name.trim()).toDomain()
}

private fun UnitEntity.toDomain() = MeasurementUnit(id = id, name = name)
