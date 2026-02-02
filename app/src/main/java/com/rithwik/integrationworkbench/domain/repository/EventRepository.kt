package com.rithwik.integrationworkbench.domain.repository

import com.rithwik.integrationworkbench.domain.model.EventRecord
import com.rithwik.integrationworkbench.plugins.AdNetwork
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    suspend fun insertEvent(event: EventRecord)
    fun observeRecentEvents(limit: Int = 2000): Flow<List<EventRecord>>
    suspend fun getLastNErrors(n: Int = 50): List<EventRecord>
    suspend fun getEventsBetween(startMs: Long, endMs: Long): List<EventRecord>
    fun observeEventsByNetwork(network: AdNetwork, limit: Int = 500): Flow<List<EventRecord>>
    suspend fun clearAll()
}
