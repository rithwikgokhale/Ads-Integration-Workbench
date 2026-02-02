# Data Dictionary

## EventRecord Schema

| Field | Type | Description |
|-------|------|-------------|
| id | String | UUID for the event |
| timestampMs | Long | Epoch timestamp in milliseconds |
| sessionId | String | Session identifier (UUID) |
| eventType | EventType | Type of event (see below) |
| status | Status | Outcome status (see below) |
| network | AdNetwork? | Ad network plugin |
| format | AdFormat? | Ad format (BANNER, INTERSTITIAL, REWARDED) |
| adUnitId | String? | Ad unit ID (redacted in exports) |
| placement | String? | Placement tag |
| latencyMs | Long? | Operation latency in milliseconds |
| networkName | String? | Network/mediation name from SDK |
| errorCode | Int? | Error code if failed |
| errorMessage | String? | Error message if failed |
| extras | Map<String, String> | Additional key-value data |
| rawPayloadJson | String? | Sanitized JSON payload |

## EventType Enum

| Value | Description |
|-------|-------------|
| INIT | SDK initialization |
| LOAD | Ad load request |
| SHOW | Ad display |
| CLICK | Ad click |
| REVENUE | Revenue/impression event |
| REWARD | Rewarded ad completion |
| ERROR | Error event |
| SYSTEM | System events (guards, consent changes) |

## Status Enum

| Value | Description |
|-------|-------------|
| SUCCESS | Operation completed successfully |
| FAILURE | Operation failed |
| PENDING | Operation in progress |
| CANCELLED | Operation was cancelled |
| NOT_IMPLEMENTED | Feature not implemented (stub plugins) |

## AdNetwork Enum

| Value | Display Name |
|-------|--------------|
| MOCK | Mock Ads |
| ADMOB | AdMob |
| UNITY | Unity Ads |
| APPLOVIN | AppLovin MAX |
| IRONSOURCE | ironSource |
| VUNGLE | Vungle |
| CHARTBOOST | Chartboost |

## AdFormat Enum

| Value | Display Name |
|-------|--------------|
| BANNER | Banner |
| INTERSTITIAL | Interstitial |
| REWARDED | Rewarded |
| REWARDED_INTERSTITIAL | Rewarded Interstitial |
| NATIVE | Native |
| APP_OPEN | App Open |

## Example Events

### Successful Init
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "timestampMs": 1700000000000,
  "sessionId": "session-uuid",
  "eventType": "INIT",
  "status": "SUCCESS",
  "network": "MOCK",
  "latencyMs": 1234,
  "networkName": "mock-network"
}
```

### Successful Load
```json
{
  "id": "b2c3d4e5-f678-90ab-cdef-123456789012",
  "timestampMs": 1700000001000,
  "sessionId": "session-uuid",
  "eventType": "LOAD",
  "status": "SUCCESS",
  "network": "MOCK",
  "format": "BANNER",
  "adUnitId": "test...unit",
  "latencyMs": 456,
  "networkName": "mock-network"
}
```

### Failed Load
```json
{
  "id": "c3d4e5f6-7890-abcd-ef12-345678901234",
  "timestampMs": 1700000002000,
  "sessionId": "session-uuid",
  "eventType": "ERROR",
  "status": "FAILURE",
  "network": "MOCK",
  "format": "INTERSTITIAL",
  "adUnitId": "INVA...D_ID",
  "errorCode": 1002,
  "errorMessage": "Simulated load failure",
  "latencyMs": 789
}
```

### System Event (Offline Blocked)
```json
{
  "id": "d4e5f678-90ab-cdef-1234-567890123456",
  "timestampMs": 1700000003000,
  "sessionId": "session-uuid",
  "eventType": "SYSTEM",
  "status": "FAILURE",
  "network": "MOCK",
  "format": "BANNER",
  "errorCode": -100,
  "errorMessage": "OFFLINE_BLOCKED"
}
```

### Rewarded Event
```json
{
  "id": "e5f67890-abcd-ef12-3456-789012345678",
  "timestampMs": 1700000004000,
  "sessionId": "session-uuid",
  "eventType": "SHOW",
  "status": "SUCCESS",
  "network": "MOCK",
  "format": "REWARDED",
  "adUnitId": "rewa...unit",
  "latencyMs": 123,
  "networkName": "mock-network",
  "extras": {
    "rewardType": "coins",
    "rewardAmount": "100"
  }
}
```

## HarnessSettings Schema

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| initTimeoutEnabled | Boolean | false | Enable init timeout guard |
| initTimeoutMs | Long | 5000 | Timeout duration in ms |
| initRetryEnabled | Boolean | false | Enable init retry |
| maxInitRetries | Int | 3 | Max retry attempts |
| badConfigInjectionEnabled | Boolean | false | Replace ad unit IDs with invalid |
| offlineGuardEnabled | Boolean | true | Block ops when offline |
| simulateLoadFailure | Boolean | false | Force load failures (mock) |
| failureRatePercent | Int | 10 | Random failure rate % (mock) |
| consentState | ConsentState | UNKNOWN | Privacy consent state |
| isAgeRestrictedUser | Boolean | false | COPPA flag |

## ConsentState Enum

| Value | Description |
|-------|-------------|
| UNKNOWN | Consent not determined |
| GRANTED | User granted consent |
| DENIED | User denied consent |
