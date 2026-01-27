# Architecture Overview

## Modules and layers
- **data**: Room database, repositories, debug bundle exporter.
- **domain**: Event model, telemetry analyzer, logging utilities.
- **ui**: Compose screens, view models, navigation.
- **ad**: MAX adapter layer, SDK manager, controllers.

## Data flow
1. UI triggers actions (init/load/show).
2. Controllers call `MaxSdkManager` and adapter.
3. Adapter emits events -> `MaxSdkManager` logs to Room via `EventLogger`.
4. UI subscribes to Room flows for timeline and insights.

## Event pipeline
- All events write to Room via a single `Event` model.
- Load latency is measured from `load()` start to callback.
- Errors, system guardrails, and revenue events share the same pipeline.
- Debug Console reads from Room for a consistent “source of truth”.

## Adapter strategy
- `MaxAdapter` interface defines stable entry points.
- `RealMaxAdapter` contains TODOs to wire AppLovin MAX SDK calls.
- `MockMaxAdapter` simulates callbacks and ad behavior for runnable builds.
- Adapter selection is automatic: real secrets → REAL mode, otherwise MOCK.
