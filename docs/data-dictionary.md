# Data Dictionary

## Event schema
| Field | Type | Description |
| --- | --- | --- |
| id | String | UUID for the event |
| timestampMs | Long | Epoch timestamp in milliseconds |
| sessionId | String | Session identifier |
| category | enum | INIT, AD_LOAD, AD_DISPLAY, AD_CLICK, AD_REVENUE, ERROR, SYSTEM |
| format | enum? | BANNER, INTERSTITIAL, REWARDED |
| adUnitId | String? | Ad unit id (stored raw, redacted on export) |
| placement | String? | Placement tag |
| latencyMs | Long? | Load latency for load events |
| networkName | String? | Network name |
| errorCode | Int? | Error code if any |
| errorMessage | String? | Error message if any |
| rawPayloadJson | String? | Sanitized payload snapshot |

## Category meanings
- **INIT**: SDK init success
- **AD_LOAD**: Load success
- **AD_DISPLAY**: Ad displayed
- **AD_CLICK**: Click event
- **AD_REVENUE**: Revenue event
- **ERROR**: Load/display/init failures
- **SYSTEM**: Guardrails (offline block, init timeout, consent changes)

## Example events (sanitized)
```json
{
  "id": "f9b2f7d9-9b5b-4f4d-8e6d-2f7b6b8e48d6",
  "timestampMs": 1700000000000,
  "sessionId": "2b6a1d",
  "category": "AD_LOAD",
  "format": "BANNER",
  "adUnitId": "abcd...wxyz",
  "latencyMs": 820,
  "networkName": "mock-network",
  "rawPayloadJson": "{\"result\":\"loaded\",\"adUnitId\":\"abcd...wxyz\"}"
}
```

```json
{
  "id": "c239d235-8d20-4f9d-9a93-67f6d1dd0b9d",
  "timestampMs": 1700000005000,
  "sessionId": "2b6a1d",
  "category": "ERROR",
  "format": "INTERSTITIAL",
  "adUnitId": "abcd...wxyz",
  "errorCode": 1001,
  "errorMessage": "Mock load failure",
  "rawPayloadJson": "{\"result\":\"load_failed\",\"adUnitId\":\"abcd...wxyz\"}"
}
```
