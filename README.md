# MAX Integration Lab (Android)

This project is a production-style Android (Kotlin) sample that demonstrates a clean AppLovin MAX integration with strong debugging/observability. It is structured for Solutions Engineering workflows: fast validation, telemetry, and issue reproduction without shipping secrets.

## What this demonstrates
- Clean MVVM + data/domain/ui separation with Room persistence.
- A MAX adapter layer that supports REAL and MOCK modes.
- A structured Event pipeline for debugging and local telemetry analysis.
- A debug bundle export flow that redacts sensitive information.

## Setup
### Prerequisites
- Android Studio (latest stable).
- JDK 17.
- Android SDK with an emulator (minSdk 26) or a physical device.

### Clone and open
```bash
git clone https://github.com/rithwikgokhale/MAX-Integration-App.git
cd MAX-Integration-App
```
Open the project in Android Studio and let Gradle sync.

### Secrets (required for REAL mode)
1. Copy `/app/src/main/assets/secrets.template.json` to `/app/src/main/assets/secrets.json`.
2. Paste your MAX SDK key and ad unit IDs:
```json
{
  "sdkKey": "PASTE_SDK_KEY_HERE",
  "bannerAdUnitId": "PASTE_BANNER_AD_UNIT_ID",
  "interstitialAdUnitId": "PASTE_INTERSTITIAL_AD_UNIT_ID",
  "rewardedAdUnitId": "PASTE_REWARDED_AD_UNIT_ID"
}
```
If `secrets.json` is missing or contains placeholders, the app runs in MOCK mode and shows the setup screen.

### Run (Android Studio)
1. Select an emulator or device (minSdk 26).
2. Click **Run**.

### Run (CLI)
```bash
./gradlew :app:assembleDebug
```
Then install the APK from Android Studio or `adb install`.

## Tabs
### Home
Quick control panel for init, load, and show actions. Shows init status, last event, ad unit IDs (redacted), and current issue repro toggles.

### Debug Console
Timeline of recent events stored in Room. Filter by category, open event details, and export a debug bundle zip.

### Insights
Local telemetry analysis computed from Room:
- success rate by format/ad unit
- p50/p95 latency by format
- top errors
- success rate drop detector (last 30 minutes vs previous 30 minutes)

### Issue Repro
Toggles and controls to reproduce common integration issues:
- Init timeout guard (5s timeout + retries)
- Bad ad unit ID injection
- Offline guard (blocks ad loads)
- Privacy/consent mock state

## Debug bundle export
The Debug Console exports a zip containing:
- `events.json` (last 2000 events)
- `app_config.json` (redacted SDK key and ad unit IDs)
- `device_info.json` (device/app info, network state)
- `last_50_errors.json`

All payloads are sanitized to remove full SDK key and ad unit IDs.

## 3–5 minute demo script
1. Launch the app in MOCK mode (leave `secrets.json` missing).
2. Tap **Initialize MAX** and watch the status change to Ready.
3. Load and show Banner + Interstitial + Rewarded to generate events.
4. Open **Debug Console**, filter to Load/Display, and open an event detail sheet.
5. Toggle **Bad Ad Unit IDs** in Issue Repro and retry a load to create errors.
6. Toggle **Init Timeout Guard**, re-init, and observe timeout handling + retries.
7. Open **Insights** to review success rates and latency summaries.
8. Export a debug bundle and share the zip.

## Build & test
```bash
./gradlew assembleDebug
./gradlew test
./gradlew ktlintCheck
./gradlew detekt
```

## Troubleshooting setup
- If Gradle fails with “Unable to locate a Java Runtime”, install JDK 17 and set `JAVA_HOME`.
- If you only need a runnable app, skip secrets and use MOCK mode.
- If you see init failures in REAL mode, confirm keys and complete `RealMaxAdapter`.

## Notes
Real MAX wiring is isolated inside `RealMaxAdapter` with TODOs pointing to the AppLovin MAX Android docs. The app is fully runnable with `MockMaxAdapter` when secrets are missing.
