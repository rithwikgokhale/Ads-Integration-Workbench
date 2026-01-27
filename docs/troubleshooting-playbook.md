# Troubleshooting Playbook

## Ads not showing
1. Confirm **SDK Mode** in Home.
2. Check **Debug Console** for recent `AD_LOAD` and `AD_DISPLAY` events.
3. If there are `ERROR` events:
   - Verify ad unit IDs in `secrets.json`.
   - Disable **Bad Ad Unit IDs** toggle.
4. If `OFFLINE_BLOCKED` appears:
   - Check device connectivity.
   - Disable **Offline Guard** to validate behavior.

## Init stuck
1. Check Home init state; look for `INIT_TIMEOUT` in Debug Console.
2. Disable **Init Timeout Guard** and retry.
3. Verify SDK key is valid and not the placeholder.
4. In REAL mode, ensure `RealMaxAdapter` is wired.

## High latency / performance drop
1. Go to **Insights** and check p95 latency.
2. Review **Drop detector** output.
3. Inspect Debug Console for spikes in `ERROR` or long `latencyMs`.
4. Suggested actions:
   - Verify network conditions.
   - Confirm ad unit IDs map to the right formats.
   - Increase ad cache size on the SDK side once wired.

## Rewarded not granting
1. Look for `RewardEarned` and `AD_DISPLAY` events in Debug Console.
2. Ensure rewarded is fully loaded before calling show.
3. In MOCK mode, verify rewarded loads and then show.
4. In REAL mode, implement rewarded callbacks in `RealMaxAdapter`.
