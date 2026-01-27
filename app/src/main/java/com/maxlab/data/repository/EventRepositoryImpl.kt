package com.maxlab.data.repository

import com.maxlab.data.db.EventDao
import com.maxlab.data.db.toDomain
import com.maxlab.data.db.toEntity
import com.maxlab.domain.model.Event
import com.maxlab.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    override suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event.toEntity())
    }

    override fun observeRecentEvents(limit: Int): Flow<List<Event>> =
        eventDao.observeRecentEvents(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLastNErrors(n: Int): List<Event> =
        eventDao.getLastNErrors(n).map { it.toDomain() }

    override suspend fun getEventsBetween(startMs: Long, endMs: Long): List<Event> =
        eventDao.getEventsBetween(startMs, endMs).map { it.toDomain() }
}
