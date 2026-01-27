# MAX Integration Lab (Android)

This project is a production-style Android (Kotlin) sample that demonstrates a clean AppLovin MAX integration with strong debugging/observability. It is structured for Solutions Engineering workflows: fast validation, telemetry, and issue reproduction without shipping secrets.

## What this demonstrates
- Clean MVVM + data/domain/ui separation with Room persistence.
- A MAX adapter layer that supports REAL and MOCK modes.
- A structured Event pipeline for debugging and local telemetry analysis.
- A debug bundle export flow that redacts sensitive information.

## Setup
1. Copy `/app/src/main/assets/secrets.template.json` to `/app/src/main/assets/secrets.json`.
2. Paste your MAX SDK key and ad unit IDs.
3. Sync the project in Android Studio and run.

If `secrets.json` is missing, the app launches in MOCK mode and shows a setup screen.

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
1. Launch the app in MOCK mode (no `secrets.json`).
2. Tap **Initialize MAX** and observe init state transition.
3. Load and show Banner + Interstitial + Rewarded to populate the timeline.
4. Open **Debug Console** and filter to Errors / Load / Display.
5. Toggle **Bad Ad Unit IDs** in Issue Repro, then trigger a load to generate errors.
6. Toggle **Init Timeout Guard**, initialize again, and observe timeout handling.
7. Open **Insights** to show latency and error summaries.
8. Export the debug bundle and share it.

## Build & test
```bash
./gradlew test
./gradlew ktlintCheck
./gradlew detekt
```

## Notes
Real MAX wiring is isolated inside `RealMaxAdapter` with TODOs pointing to the AppLovin MAX Android docs. The app is fully runnable with `MockMaxAdapter` when secrets are missing.
