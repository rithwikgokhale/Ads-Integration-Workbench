# Ads Integration Workbench (Android)

A production-style Android (Kotlin) workbench for testing, debugging, and validating multiple ad network integrations. This project demonstrates clean architecture, plugin-based SDK abstraction, local telemetry analysis, and comprehensive debugging/observability tools.

## What this demonstrates
- **Plugin System**: Extensible architecture supporting multiple ad networks (Mock, AdMob, Unity Ads, AppLovin MAX, etc.)
- **Clean MVVM + data/domain/ui separation** with Room persistence and DataStore
- **Structured Event pipeline** for debugging and local telemetry analysis
- **Debug bundle export** with automatic secret redaction
- **Issue reproduction harness** with configurable guards and failure injection

## Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android SDK with an emulator (minSdk 26) or a physical device

## Setup

### Clone and open
```bash
git clone https://github.com/rithwikgokhale/Ads-Integration-Workbench.git
cd Ads-Integration-Workbench
```
Open the project in Android Studio and let Gradle sync.

### Plugin Configuration
Configure ad network credentials in the **Integrations** tab:
1. Select a plugin (Mock, AdMob, Unity Ads, AppLovin MAX)
2. Enter App ID / SDK Key
3. Enter Ad Unit IDs for each format
4. Save the configuration

The **Mock plugin** works without any configuration and is ideal for testing the UI and event pipeline.

### Run (Android Studio)
1. Select an emulator or device (minSdk 26)
2. Click **Run**

### Run (CLI)
```bash
./gradlew :app:assembleDebug
```
Then install the APK via `adb install`.

## Tabs

### Integrations
- Configure ad network plugins (credentials, ad unit IDs)
- Harness settings: init timeout guard, retry policy, bad config injection, offline guard, consent state

### Actions
- Select a plugin and execute actions (Initialize, Load, Show)
- View plugin state and action results
- Banner placeholder for mock banner display

### Debug Console
- Live timeline of events stored in Room
- Filter by event type (Init/Load/Show/Error/System) and status (Success/Failure)
- Filter by ad network
- Tap event row for full details in bottom sheet
- Export debug bundle (zip) with share intent

### Insights
- Success rate by network/format
- p50/p95 latency by format and network
- Top errors grouped by code/message
- Drop detector: flags >20% success rate drop in 30-minute window

## Debug Bundle Export
The Debug Console exports a zip containing:
- `events.json` (last 2000 events)
- `app_config.json` (harness settings, no secrets)
- `device_info.json` (device/app info, network state)
- `last_50_errors.json`
- `plugin_config_redacted.json` (credentials and ad unit IDs redacted)

All payloads are automatically sanitized to remove sensitive information.

## Issue Reproduction Harness
Toggle these in the **Integrations** tab:
- **Init Timeout Guard**: 5s timeout with exponential backoff retry (1s, 2s, 4s)
- **Init Retry**: Enable automatic retry on init timeout
- **Bad Config Injection**: Replace ad unit IDs with invalid values
- **Offline Guard**: Block ad operations when device is offline
- **Simulate Load Failure**: Force load failures in mock plugin
- **Consent State**: Set UNKNOWN / GRANTED / DENIED
- **Age Restricted User**: Toggle COPPA flag

## 3–5 Minute Demo Script
1. Launch the app
2. Go to **Integrations** tab, select Mock plugin (no configuration needed)
3. Go to **Actions** tab, select Mock, tap **Initialize**
4. Tap **Load Banner**, then **Show Banner** to see placeholder
5. Tap **Load Interstitial**, then **Show Interstitial**
6. Go to **Debug Console**, filter by Load/Show events
7. Tap an event to see full details
8. Go back to **Integrations**, enable **Bad Config Injection**
9. Go to **Actions**, try **Load Banner** again - observe failure
10. Go to **Debug Console**, filter by Error events
11. Go to **Insights** to see success rates and error summaries
12. Tap **Export Bundle** to share debug data

## Build & Test
```bash
./gradlew assembleDebug
./gradlew test
./gradlew ktlintCheck
./gradlew detekt
```

## Troubleshooting
- **Gradle wrapper failure**: If you see `NoClassDefFoundError: org/gradle/wrapper/IDownload`, re-download the gradle wrapper jar or use a local Gradle installation
- **JDK not found**: Install JDK 17 and set `JAVA_HOME`
- **Plugin stubs return NOT_IMPLEMENTED**: This is expected for AdMob/Unity/AppLovin until SDKs are wired

## Architecture
See [docs/architecture.md](docs/architecture.md) for module breakdown, data flow, and plugin system design.

## Plugin Authoring
See [docs/plugin-authoring-guide.md](docs/plugin-authoring-guide.md) for how to add new ad network plugins.

## Troubleshooting Playbook
See [docs/troubleshooting-playbook.md](docs/troubleshooting-playbook.md) for decision trees on common issues.

## Data Dictionary
See [docs/data-dictionary.md](docs/data-dictionary.md) for event schema and field definitions.
