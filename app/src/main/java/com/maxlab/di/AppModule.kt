package com.maxlab.di

import android.content.Context
import androidx.room.Room
import com.maxlab.core.Clock
import com.maxlab.core.IdGenerator
import com.maxlab.core.SystemClock
import com.maxlab.core.UuidGenerator
import com.maxlab.data.db.AppDatabase
import com.maxlab.data.db.EventDao
import com.maxlab.data.repository.EventRepositoryImpl
import com.maxlab.domain.repository.EventRepository
import com.maxlab.domain.telemetry.TelemetryAnalyzer
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "maxlab.db").build()
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    @Provides
    fun provideClock(): Clock = SystemClock()

    @Provides
    fun provideIdGenerator(): IdGenerator = UuidGenerator()

    @Provides
    fun provideTelemetryAnalyzer(): TelemetryAnalyzer = TelemetryAnalyzer()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository
}
