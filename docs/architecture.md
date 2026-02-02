# Architecture Overview

## Module Structure

```
com.rithwik.integrationworkbench/
├── plugins/                    # Ad network plugin system
│   ├── AdsIntegrationPlugin.kt # Core interface and models
│   ├── PluginRegistry.kt       # Plugin discovery and lookup
│   ├── mock/                   # Mock plugin implementation
│   ├── admob/                  # AdMob stub
│   └── unity/                  # Unity Ads stub
├── domain/                     # Business logic
│   ├── model/                  # Domain models (EventRecord, HarnessSettings)
│   ├── logging/                # EventLogger, Sanitizer, SessionProvider
│   ├── telemetry/              # TelemetryAnalyzer
│   └── repository/             # Repository interfaces
├── data/                       # Data layer
│   ├── db/                     # Room database
│   ├── repository/             # Repository implementations
│   └── exporter/               # Debug bundle exporter
├── core/                       # Shared utilities
│   ├── Clock.kt                # Time abstraction
│   ├── IdGenerator.kt          # UUID generation
│   ├── NetworkMonitor.kt       # Connectivity checking
│   └── HarnessEnv.kt           # Harness environment provider
├── di/                         # Hilt dependency injection
│   └── AppModule.kt            # Module definitions
└── ui/                         # Presentation layer
    ├── WorkbenchAppRoot.kt     # Main navigation
    ├── integrations/           # Plugin configuration screen
    ├── actions/                # Action execution screen
    ├── debug/                  # Debug console screen
    ├── insights/               # Telemetry insights screen
    └── common/                 # Shared UI utilities
```

## Plugin System

### Core Interface
`AdsIntegrationPlugin` defines the contract for all ad network integrations:
- `network`: Identifies the ad network
- `supportedFormats`: Set of supported ad formats
- `state`: Observable plugin state flow
- `configure()`: Set credentials and ad unit IDs
- `initialize()`: Initialize the SDK
- `execute(action)`: Execute load/show actions
- `healthCheck()`: Return current health status

### Plugin Registry
`PluginRegistry` uses Hilt multibinding to collect all plugins:
```kotlin
@Module
abstract class PluginModule {
    @Binds @IntoSet
    abstract fun bindMockPlugin(plugin: MockAdsPlugin): AdsIntegrationPlugin
}
```

### Adapter Strategy
- **MockAdsPlugin**: Full simulation, works without SDK
- **AdMobPluginStub**: Returns NOT_IMPLEMENTED until SDK wired
- **UnityAdsPluginStub**: Returns NOT_IMPLEMENTED until SDK wired

## Data Flow

```
User Action
    ↓
ViewModel (ActionsViewModel)
    ↓
HarnessEnv (applies guards)
    ↓
Plugin.execute(action)
    ↓
EventLogger.log()
    ↓
EventRepository.insert()
    ↓
Room Database
    ↓
UI observes Flow<List<EventRecord>>
```

## Event Pipeline

1. **Action Execution**: ViewModel calls plugin via HarnessEnv
2. **Result Handling**: PluginActionResult converted to EventRecord
3. **Sanitization**: EventSanitizer redacts sensitive data
4. **Persistence**: EventRepository writes to Room
5. **Observation**: UI screens observe event flows

## Harness Environment

`HarnessEnv` provides:
- **Offline Guard**: Blocks operations when offline
- **Init Timeout**: Wraps init in configurable timeout
- **Retry Policy**: Exponential backoff (1s, 2s, 4s)
- **Bad Config Injection**: Replaces ad unit IDs with invalid values

## Settings Storage

DataStore Preferences stores:
- Harness settings (timeouts, guards, consent)
- Plugin configurations (credentials, ad unit IDs)

## Debug Bundle Export

`DebugBundleExporter` creates a ZIP containing:
- events.json: Last 2000 events
- app_config.json: Harness settings (no secrets)
- device_info.json: Device/app metadata
- last_50_errors.json: Recent failures
- plugin_config_redacted.json: Configs with redacted secrets

## Testing Strategy

- **TelemetryAnalyzer**: Unit tests for percentile, success rate, drop detection
- **MockAdsPlugin**: Unit tests for state transitions and action results
- **ViewModels**: Tests for plugin selection and action execution
