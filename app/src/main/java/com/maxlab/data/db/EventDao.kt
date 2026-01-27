package com.maxlab.data.db

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

    @Query("SELECT * FROM events WHERE category = 'ERROR' ORDER BY timestampMs DESC LIMIT :limit")
    suspend fun getLastNErrors(limit: Int): List<EventEntity>

    @Query("SELECT * FROM events WHERE timestampMs BETWEEN :startMs AND :endMs ORDER BY timestampMs DESC")
    suspend fun getEventsBetween(startMs: Long, endMs: Long): List<EventEntity>
}
