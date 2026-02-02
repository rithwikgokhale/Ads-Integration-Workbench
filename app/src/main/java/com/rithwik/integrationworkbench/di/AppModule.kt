package com.rithwik.integrationworkbench.di

import android.content.Context
import androidx.room.Room
import com.rithwik.integrationworkbench.core.Clock
import com.rithwik.integrationworkbench.core.IdGenerator
import com.rithwik.integrationworkbench.core.SystemClock
import com.rithwik.integrationworkbench.core.UuidGenerator
import com.rithwik.integrationworkbench.data.db.AppDatabase
import com.rithwik.integrationworkbench.data.db.EventDao
import com.rithwik.integrationworkbench.data.repository.EventRepositoryImpl
import com.rithwik.integrationworkbench.data.repository.SettingsRepositoryImpl
import com.rithwik.integrationworkbench.domain.logging.EventLogger
import com.rithwik.integrationworkbench.domain.logging.EventLoggerImpl
import com.rithwik.integrationworkbench.domain.repository.EventRepository
import com.rithwik.integrationworkbench.domain.repository.SettingsRepository
import com.rithwik.integrationworkbench.domain.telemetry.TelemetryAnalyzer
import com.rithwik.integrationworkbench.plugins.AdsIntegrationPlugin
import com.rithwik.integrationworkbench.plugins.admob.AdMobPluginStub
import com.rithwik.integrationworkbench.plugins.mock.MockAdsPlugin
import com.rithwik.integrationworkbench.plugins.unity.UnityAdsPluginStub
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
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
        return Room.databaseBuilder(context, AppDatabase::class.java, "workbench.db").build()
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

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindEventLogger(impl: EventLoggerImpl): EventLogger
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PluginModule {
    @Binds
    @IntoSet
    abstract fun bindMockPlugin(plugin: MockAdsPlugin): AdsIntegrationPlugin

    @Binds
    @IntoSet
    abstract fun bindAdMobPlugin(plugin: AdMobPluginStub): AdsIntegrationPlugin

    @Binds
    @IntoSet
    abstract fun bindUnityPlugin(plugin: UnityAdsPluginStub): AdsIntegrationPlugin
}
