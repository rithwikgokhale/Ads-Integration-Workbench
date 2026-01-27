package com.maxlab.domain.repository

import com.maxlab.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insertEvent(event: Event)
    fun observeRecentEvents(limit: Int = 2000): Flow<List<Event>>
    suspend fun getLastNErrors(n: Int = 50): List<Event>
    suspend fun getEventsBetween(startMs: Long, endMs: Long): List<Event>
}
