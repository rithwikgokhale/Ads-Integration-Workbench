package com.maxlab.ad

import com.maxlab.core.sanitizePayload
import com.maxlab.core.Clock
import com.maxlab.data.repository.SecretsRepository
import com.maxlab.data.repository.SettingsRepository
import com.maxlab.domain.logging.EventLogger
import com.maxlab.domain.model.EventCategory
import com.maxlab.domain.model.InitState
import com.maxlab.domain.model.SdkMode
import com.maxlab.domain.model.SecretsSource
import com.maxlab.domain.model.SecretsStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaxSdkManager @Inject constructor(
    private val secretsRepository: SecretsRepository,
    private val settingsRepository: SettingsRepository,
    private val eventLogger: EventLogger,
    private val clock: Clock
) : MaxAdapterListener {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val initStateFlow = MutableStateFlow<InitState>(InitState.NotStarted)
    private val sdkModeFlow = MutableStateFlow(SdkMode.MOCK)
    private val secretsFlow = MutableStateFlow(SecretsStatus(config = null, source = SecretsSource.MISSING))
    private val eventFlowInternal = MutableSharedFlow<MaxEvent>(extraBufferCapacity = 32)

    private var initDeferred: CompletableDeferred<InitState>? = null
    private var adapter: MaxAdapter = MockMaxAdapter()
    private val loadStartTimes: MutableMap<String, Long> = mutableMapOf()
    private var lastConsentState: com.maxlab.domain.model.ConsentState? = null
    private var lastAgeRestricted: Boolean? = null

    val initState: StateFlow<InitState> = initStateFlow.asStateFlow()
    val sdkMode: StateFlow<SdkMode> = sdkModeFlow.asStateFlow()
    val secretsStatus: StateFlow<SecretsStatus> = secretsFlow.asStateFlow()
    val adapterEvents: SharedFlow<MaxEvent> = eventFlowInternal

    fun currentAdapter(): MaxAdapter = adapter

    fun recordLoadStart(format: com.maxlab.domain.model.AdFormat, adUnitId: String) {
        loadStartTimes["${format.name}::$adUnitId"] = clock.nowMs()
    }

    init {
        refreshSecrets()
        scope.launch {
            settingsRepository.settingsFlow.collectLatest { settings ->
                adapter.setPrivacy(settings.consentState, settings.isAgeRestrictedUser)
                if (lastConsentState != settings.consentState || lastAgeRestricted != settings.isAgeRestrictedUser) {
                    eventLogger.log(
                        category = EventCategory.SYSTEM,
                        errorMessage = "CONSENT_STATE_CHANGED",
                        rawPayloadJson = """{"consentState":"${settings.consentState}","isAgeRestrictedUser":${settings.isAgeRestrictedUser}}"""
                    )
                }
                lastConsentState = settings.consentState
                lastAgeRestricted = settings.isAgeRestrictedUser
            }
        }
    }

    fun refreshSecrets() {
        val status = secretsRepository.loadSecrets()
        secretsFlow.value = status
        val config = status.config
        val hasRealSecrets = status.source == SecretsSource.REAL &&
            config?.sdkKey?.isNotBlank() == true &&
            config.sdkKey != "PASTE_SDK_KEY_HERE"
        adapter = if (hasRealSecrets) RealMaxAdapter() else MockMaxAdapter()
        adapter.setListener(this)
        sdkModeFlow.value = if (hasRealSecrets) SdkMode.REAL else SdkMode.MOCK
    }

    suspend fun initialize() {
        refreshSecrets()
        val config = secretsFlow.value.config
        if (config == null || config.sdkKey.isBlank() || config.sdkKey == "PASTE_SDK_KEY_HERE") {
            initStateFlow.value = InitState.Failed("Missing SDK key (secrets.json)")
            return
        }
        val settings = settingsRepository.currentSettings()
        if (sdkModeFlow.value == SdkMode.MOCK && settings.initTimeoutGuardEnabled) {
            adapter = MockMaxAdapter(initDelayMs = 6500L)
            adapter.setListener(this)
        }
        val maxAttempts = if (settings.initTimeoutGuardEnabled) 3 else 1
        val backoff = listOf(1000L, 2000L, 4000L)
        repeat(maxAttempts) { attempt ->
            initStateFlow.value = InitState.Initializing
            initDeferred = CompletableDeferred()
            adapter.initialize(config.sdkKey)
            val result = if (settings.initTimeoutGuardEnabled) {
                withTimeoutOrNull(5000L) { initDeferred?.await() }
            } else {
                initDeferred?.await()
            }
            if (result is InitState.Ready) return
            if (result == null) {
                eventLogger.log(
                    category = EventCategory.SYSTEM,
                    errorMessage = "INIT_TIMEOUT",
                    rawPayloadJson = """{"timeoutMs":5000}"""
                )
                initStateFlow.value = InitState.Failed("Initialization timed out")
            }
            if (attempt < maxAttempts - 1) {
                delay(backoff[attempt])
            }
        }
    }

    override fun onEvent(event: MaxEvent) {
        scope.launch {
            eventFlowInternal.emit(event)
        }
        when (event) {
            is MaxEvent.InitSuccess -> {
                initStateFlow.value = InitState.Ready
                initDeferred?.complete(InitState.Ready)
                eventLogger.log(
                    category = EventCategory.INIT,
                    networkName = event.networkName,
                    rawPayloadJson = """{"result":"success","networkName":"${event.networkName}"}"""
                )
            }
            is MaxEvent.InitFailure -> {
                val message = event.error.message
                initStateFlow.value = InitState.Failed(message)
                initDeferred?.complete(InitState.Failed(message))
                eventLogger.log(
                    category = EventCategory.ERROR,
                    errorCode = event.error.code,
                    errorMessage = message,
                    rawPayloadJson = """{"result":"failure","errorCode":${event.error.code},"errorMessage":"$message"}"""
                )
            }
            is MaxEvent.AdLoaded -> {
                val latency = loadStartTimes.remove("${event.format.name}::${event.adUnitId}")?.let {
                    clock.nowMs() - it
                }
                eventLogger.log(
                    category = EventCategory.AD_LOAD,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    latencyMs = latency,
                    networkName = event.networkName,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"loaded","adUnitId":"${event.adUnitId}","networkName":"${event.networkName}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.AdLoadFailed -> {
                val latency = loadStartTimes.remove("${event.format.name}::${event.adUnitId}")?.let {
                    clock.nowMs() - it
                }
                eventLogger.log(
                    category = EventCategory.ERROR,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    latencyMs = latency,
                    errorCode = event.error.code,
                    errorMessage = event.error.message,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"load_failed","adUnitId":"${event.adUnitId}","errorCode":${event.error.code},"errorMessage":"${event.error.message}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.AdDisplayed -> {
                eventLogger.log(
                    category = EventCategory.AD_DISPLAY,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"displayed","adUnitId":"${event.adUnitId}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.AdDisplayFailed -> {
                eventLogger.log(
                    category = EventCategory.ERROR,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    errorCode = event.error.code,
                    errorMessage = event.error.message,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"display_failed","adUnitId":"${event.adUnitId}","errorCode":${event.error.code},"errorMessage":"${event.error.message}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.AdClicked -> {
                eventLogger.log(
                    category = EventCategory.AD_CLICK,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"clicked","adUnitId":"${event.adUnitId}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.AdRevenue -> {
                eventLogger.log(
                    category = EventCategory.AD_REVENUE,
                    format = event.format,
                    adUnitId = event.adUnitId,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"revenue","adUnitId":"${event.adUnitId}","revenue":${event.revenue},"source":"mock"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
            is MaxEvent.RewardEarned -> {
                eventLogger.log(
                    category = EventCategory.SYSTEM,
                    format = com.maxlab.domain.model.AdFormat.REWARDED,
                    adUnitId = event.adUnitId,
                    rawPayloadJson = sanitizePayload(
                        """{"result":"reward_earned","adUnitId":"${event.adUnitId}"}""",
                        listOf(event.adUnitId)
                    )
                )
            }
        }
    }
}
