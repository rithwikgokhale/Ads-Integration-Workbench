package com.maxlab.data.repository

import android.content.Context
import com.maxlab.domain.model.SecretsConfig
import com.maxlab.domain.model.SecretsSource
import com.maxlab.domain.model.SecretsStatus
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecretsRepository @Inject constructor(
    private val context: Context,
    private val json: Json
) {
    fun loadSecrets(): SecretsStatus {
        val assetManager = context.assets
        return try {
            assetManager.open("secrets.json").use { input ->
                val text = input.bufferedReader().readText()
                val config = json.decodeFromString(SecretsConfig.serializer(), text)
                SecretsStatus(config = config, source = SecretsSource.REAL)
            }
        } catch (missing: IOException) {
            loadTemplateFallback()
        } catch (exception: Exception) {
            SecretsStatus(config = null, source = SecretsSource.MISSING, errorMessage = exception.message)
        }
    }

    private fun loadTemplateFallback(): SecretsStatus =
        try {
            context.assets.open("secrets.template.json").use { input ->
                val text = input.bufferedReader().readText()
                val config = json.decodeFromString(SecretsConfig.serializer(), text)
                SecretsStatus(config = config, source = SecretsSource.TEMPLATE)
            }
        } catch (exception: Exception) {
            SecretsStatus(config = null, source = SecretsSource.MISSING, errorMessage = exception.message)
        }
}
