package com.rithwik.integrationworkbench.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY timestampMs DESC LIMIT :limit")
    fun observeRecentEvents(limit: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE status = 'FAILURE' ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun getLastNErrors(limit: Int): List<EventEntity>

    @Query("SELECT * FROM events WHERE timestampMs BETWEEN :startMs AND :endMs ORDER BY timestampMs DESC")
    suspend fun getEventsBetween(startMs: Long, endMs: Long): List<EventEntity>

    @Query("SELECT * FROM events WHERE network = :network ORDER BY timestampMs DESC LIMIT :limit")
    fun observeEventsByNetwork(network: String, limit: Int): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE eventType = :eventType ORDER BY timestampMs DESC LIMIT :limit")
    fun observeEventsByType(eventType: String, limit: Int): Flow<List<EventEntity>>

    @Query("DELETE FROM events")
    suspend fun clearAll()
}
