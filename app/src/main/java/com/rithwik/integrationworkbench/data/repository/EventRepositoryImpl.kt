package com.rithwik.integrationworkbench.data.repository

import com.rithwik.integrationworkbench.data.db.EventDao
import com.rithwik.integrationworkbench.data.db.toDomain
import com.rithwik.integrationworkbench.data.db.toEntity
import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.plugins.AdNetwork
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    override suspend fun insertEvent(event: EventRecord) {
        eventDao.insertEvent(event.toEntity())
    }

    override fun observeRecentEvents(limit: Int): Flow<List<EventRecord>> =
        eventDao.observeRecentEvents(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLastNErrors(n: Int): List<EventRecord> =
        eventDao.getLastNErrors(n).map { it.toDomain() }

    override suspend fun getEventsBetween(startMs: Long, endMs: Long): List<EventRecord> =
        eventDao.getEventsBetween(startMs, endMs).map { it.toDomain() }

    override fun observeEventsByNetwork(network: AdNetwork, limit: Int): Flow<List<EventRecord>> =
        eventDao.observeEventsByNetwork(network.name, limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun clearAll() {
        eventDao.clearAll()
    }
}
