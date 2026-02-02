# Troubleshooting Playbook

## Ads Not Showing

### Decision Tree
1. **Is the plugin initialized?**
   - Check Actions screen: Plugin State shows "Initialized: true"
   - If false → Initialize the plugin first

2. **Is the ad loaded?**
   - Check Actions screen: Ad State for format shows "Loaded"
   - If not → Load the ad before showing

3. **Check Debug Console for errors**
   - Filter by ERROR events
   - Look for error codes and messages

4. **Is offline guard enabled?**
   - Go to Integrations → Harness Settings
   - If Offline Guard is ON and device is offline, loads are blocked

5. **Is bad config injection enabled?**
   - Go to Integrations → Harness Settings
   - If enabled, ad unit IDs are replaced with invalid values

### Likely Root Causes
- Plugin not initialized
- Ad not loaded before show
- Invalid ad unit IDs
- Network offline with guard enabled
- SDK not properly configured (for real SDKs)

---

## Init Stuck / Timeout

### Decision Tree
1. **Is init timeout guard enabled?**
   - Check Integrations → Harness Settings
   - If enabled, init times out after 5s

2. **Check Debug Console for INIT_TIMEOUT events**
   - Filter by SYSTEM category
   - Look for "INIT_TIMEOUT" error message

3. **Is retry enabled?**
   - If init retry is ON, check for multiple attempts
   - After max retries, init fails

4. **Is the device online?**
   - Some SDKs require network for initialization

### Actions
- Disable init timeout guard for testing
- Check SDK documentation for init requirements
- Verify credentials/app ID is correct

---

## High Latency / Performance Drop

### Decision Tree
1. **Go to Insights screen**
   - Check p50/p95 latency by format and network

2. **Check Drop Detector**
   - If alert shows, success rate dropped >20% in 30 min

3. **Analyze Debug Console**
   - Filter by LOAD events
   - Look at latencyMs values
   - Identify patterns (specific format, time of day)

### Likely Root Causes
- Network congestion
- Server-side issues
- Inefficient ad waterfall configuration
- Device resource constraints

### Actions
- Check network quality
- Review ad unit configuration
- Consider ad caching strategies

---

## Rewarded Not Granting

### Decision Tree
1. **Did the rewarded ad show successfully?**
   - Check Debug Console for SHOW event with SUCCESS status

2. **Was the ad watched to completion?**
   - Mock plugin always grants reward
   - Real SDKs require watching full video

3. **Check for REWARD events**
   - Filter Debug Console by REWARD type
   - Verify reward callback was logged

### Likely Root Causes
- Ad dismissed before completion
- SDK callback not implemented
- App logic not handling reward event

---

## Plugin Returns NOT_IMPLEMENTED

### Explanation
This is expected for stub plugins (AdMob, Unity) until the real SDK is wired.

### Actions
- Use Mock plugin for testing UI/event pipeline
- See [plugin-authoring-guide.md](plugin-authoring-guide.md) to wire real SDK

---

## Debug Bundle Export Issues

### File Not Created
- Check app has storage permissions
- Check cache directory is accessible

### Share Intent Not Opening
- Verify FileProvider is configured in AndroidManifest
- Check file_paths.xml includes cache directory

---

## Build/Gradle Issues

### NoClassDefFoundError: org/gradle/wrapper/IDownload
- Gradle wrapper jar may be corrupted
- Solution: Delete gradle/wrapper/gradle-wrapper.jar and re-run wrapper task
- Or use local Gradle installation: `gradle wrapper`

### JDK Not Found
- Install JDK 17
- Set JAVA_HOME environment variable
- Verify: `java -version` shows Java 17
