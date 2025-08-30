# Android Gesture Implementation

## Overview

Fate/Grand Automata uses Android's AccessibilityService framework to perform automated gestures like clicks, swipes, and complex touch sequences. The gesture system is designed to simulate human-like interactions while providing precise control over timing and movement patterns.

## Core Architecture

### 1. AccessibilityService Foundation

#### TapperService
The main accessibility service that provides gesture capabilities:

```kotlin
class TapperService : AccessibilityService() {
    // Extends Android's AccessibilityService
    // Provides gesture dispatch capabilities
    // Manages service lifecycle and permissions
}
```

**Key Responsibilities:**
- **Service Management**: Handles accessibility service lifecycle
- **Permission Handling**: Manages accessibility permissions and setup
- **Gesture Coordination**: Coordinates gesture execution with other app components
- **System Integration**: Integrates with Android's accessibility framework

### 2. Gesture Implementation

#### AccessibilityGestures Class
The core gesture implementation that handles all touch interactions:

```kotlin
class AccessibilityGestures @Inject constructor(
    private val gesturePrefs: IGesturesPreferences,
    private val wait: Waiter
) : GestureService
```

**Platform-Specific Implementations:**
- **Android 7 (API 24)**: Basic gesture support with single-stroke gestures
- **Android 8+ (API 26+)**: Advanced continued gestures for more precise control

## Gesture Types

### 1. Click Gestures

#### Basic Click Implementation
```kotlin
override fun click(location: Location, times: Int) = runBlocking {
    val swipePath = Path().moveTo(location)
    
    val stroke = GestureDescription.StrokeDescription(
        swipePath,
        gesturePrefs.clickDelay.inWholeMilliseconds,
        gesturePrefs.clickDuration.inWholeMilliseconds
    )
    
    repeat(times) {
        performGesture(stroke)
    }
    
    wait(gesturePrefs.clickWaitTime)
}
```

**Click Parameters:**
- **Click Delay**: Time before click starts (gesture preparation)
- **Click Duration**: How long the "finger" stays down
- **Click Wait Time**: Delay after click completes before next action
- **Multiple Clicks**: Support for repeated clicks at same location

**Path Construction:**
```kotlin
fun Path.moveTo(location: Location) = apply {
    moveTo(location.x.toFloat(), location.y.toFloat())
}
```

### 2. Swipe Gestures

The swipe implementation varies based on Android version to optimize compatibility and precision.

#### Android 7 Swipe (API 24)
```kotlin
suspend fun swipe7(start: Location, end: Location) {
    val swipePath = Path()
        .moveTo(start)
        .lineTo(end)
    
    val swipeStroke = GestureDescription.StrokeDescription(
        swipePath,
        0,
        gesturePrefs.swipeDuration.inWholeMilliseconds
    )
    performGesture(swipeStroke)
    
    wait(gesturePrefs.swipeWaitTime)
}
```

**Android 7 Characteristics:**
- **Single Stroke**: Entire swipe as one continuous gesture
- **Flick-like Behavior**: Similar to quick finger flick
- **Distance Limitations**: Long swipes may not be detected correctly by FGO
- **Immediate Execution**: Direct path from start to end point

#### Android 8+ Swipe (API 26+)
```kotlin
@RequiresApi(Build.VERSION_CODES.O)
suspend fun swipe8(start: Location, end: Location) {
    val xDiff = (end.x - start.x).toFloat()
    val yDiff = (end.y - start.y).toFloat()
    val direction = atan2(xDiff, yDiff)
    var distanceLeft = sqrt(xDiff.pow(2) + yDiff.pow(2))
    
    val swipeDelay = 1L
    val swipeDuration = 1L
    
    val timesToSwipe = gesturePrefs.swipeDuration.inWholeMilliseconds / (swipeDelay + swipeDuration)
    val thresholdDistance = distanceLeft / timesToSwipe
    
    var from = start
    val mouseDownPath = Path().moveTo(start)
    
    var lastStroke = GestureDescription.StrokeDescription(
        mouseDownPath,
        0,
        200L,
        true  // willContinue = true
    ).also {
        performGesture(it)
    }
    
    // Continue with multiple small swipe segments...
}
```

**Android 8+ Characteristics:**
- **Continued Gestures**: Multiple connected stroke segments
- **Human-like Movement**: Simulates natural finger movement patterns
- **Precise Control**: Better accuracy for complex swipe patterns
- **Incremental Steps**: Breaks long swipes into smaller segments

**Continued Gesture Flow:**
1. **Initial Touch**: Finger down event with `willContinue = true`
2. **Movement Segments**: Series of small movements maintaining finger contact
3. **Final Lift**: Last segment with `willContinue = false` to lift finger

### 3. Advanced Gesture Features

#### Path Construction Utilities
```kotlin
fun Path.moveTo(location: Location) = apply {
    moveTo(location.x.toFloat(), location.y.toFloat())
}

fun Path.lineTo(location: Location) = apply {
    lineTo(location.x.toFloat(), location.y.toFloat())
}
```

#### Gesture Execution Pipeline
```kotlin
private suspend fun performGesture(strokeDesc: GestureDescription.StrokeDescription): Boolean = 
    suspendCancellableCoroutine { continuation ->
        val gestureDesc = GestureDescription.Builder()
            .addStroke(strokeDesc)
            .build()
        
        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                continuation.resume(true)
            }
            
            override fun onCancelled(gestureDescription: GestureDescription?) {
                continuation.resume(false)
            }
        }
        
        service?.dispatchGesture(gestureDesc, callback, null)
    }
```

## Configuration System

### 1. Gesture Preferences

#### IGesturesPreferences Interface
```kotlin
interface IGesturesPreferences {
    val clickWaitTime: Duration
    val clickDuration: Duration  
    val clickDelay: Duration
    val swipeWaitTime: Duration
    val swipeDuration: Duration
}
```

**Timing Configurations:**
- **Click Duration**: How long finger stays pressed (typically 50-200ms)
- **Click Delay**: Preparation time before click (typically 0-100ms)
- **Click Wait Time**: Delay after click before next action (typically 100-500ms)
- **Swipe Duration**: Total time for swipe movement (typically 300-1000ms)
- **Swipe Wait Time**: Delay after swipe completion (typically 200-800ms)

### 2. Adaptive Timing

The gesture system supports adaptive timing based on:
- **Device Performance**: Slower devices get longer delays
- **Game Responsiveness**: Adjustable based on FGO's response times
- **User Preferences**: Customizable for different play styles
- **Script Requirements**: Different scripts may need different timing

## Platform Integration

### 1. Version-Specific Handling

```kotlin
override fun swipe(start: Location, end: Location) = runBlocking {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        swipe8(start, end)
    } else {
        swipe7(start, end)
    }
}
```

**Benefits of Version-Specific Implementation:**
- **Compatibility**: Works on older Android versions
- **Optimization**: Uses best available API for each platform
- **Reliability**: Fallback mechanisms for different Android behaviors

### 2. AccessibilityService Integration

#### Service Lifecycle Management
```kotlin
// Service activation and deactivation
// Permission handling and user guidance
// Resource cleanup and memory management
// Integration with app UI and notifications
```

#### Gesture Coordination
- **Queue Management**: Ensures gestures execute in proper sequence
- **Conflict Resolution**: Prevents overlapping or conflicting gestures
- **Error Recovery**: Handles failed gestures and retry logic
- **Performance Monitoring**: Tracks gesture success rates and timing

## Human-Like Behavior Simulation

### 1. Natural Timing Patterns

**Variable Delays:**
- **Randomization**: Small random variations in timing
- **Realistic Intervals**: Delays that match human reaction times
- **Context Awareness**: Different timing for different UI elements

**Movement Characteristics:**
- **Curved Paths**: Natural finger movement rather than straight lines
- **Velocity Variation**: Acceleration and deceleration patterns
- **Touch Pressure**: Simulated pressure variations where supported

### 2. Anti-Detection Features

**Behavioral Patterns:**
- **Irregular Timing**: Avoiding perfectly consistent timing patterns
- **Natural Variations**: Small deviations in click locations
- **Realistic Sequences**: Following logical interaction patterns
- **Rest Periods**: Occasional pauses that mimic human breaks

## Error Handling and Recovery

### 1. Gesture Failure Detection

```kotlin
val callback = object : AccessibilityService.GestureResultCallback() {
    override fun onCompleted(gestureDescription: GestureDescription?) {
        // Gesture succeeded
        continuation.resume(true)
    }
    
    override fun onCancelled(gestureDescription: GestureDescription?) {
        // Gesture failed or was cancelled
        continuation.resume(false)
    }
}
```

### 2. Recovery Strategies

**Retry Logic:**
- **Automatic Retries**: Retry failed gestures with backoff
- **Alternative Approaches**: Try different gesture parameters
- **Fallback Methods**: Switch to different gesture types if needed
- **User Notification**: Alert user to persistent failures

**Error Scenarios:**
- **Service Disconnection**: AccessibilityService becomes unavailable
- **Permission Loss**: User revokes accessibility permissions
- **System Conflicts**: Other apps interfering with gestures
- **Hardware Issues**: Touch screen or system responsiveness problems

## Performance Optimization

### 1. Efficiency Measures

**Resource Management:**
- **Object Reuse**: Reusing Path and GestureDescription objects
- **Memory Optimization**: Efficient cleanup of gesture resources
- **Background Processing**: Non-blocking gesture execution
- **Batch Operations**: Grouping related gestures when possible

**Timing Optimization:**
- **Predictive Scheduling**: Pre-calculating gesture timing
- **Adaptive Delays**: Adjusting delays based on system performance
- **Queue Optimization**: Efficient gesture queue management
- **Parallel Execution**: Multiple gestures when appropriate

### 2. System Integration

**Android Integration:**
- **System UI**: Proper interaction with Android system UI
- **Notification System**: Status updates and error notifications
- **Background Compatibility**: Works when app is in background
- **Multi-tasking**: Compatible with other running apps

## Debugging and Monitoring

### 1. Gesture Logging

```kotlin
Timber.d("click $location x$times")
// Comprehensive logging of gesture parameters
// Success/failure tracking
// Performance metrics collection
// Debug information for troubleshooting
```

### 2. Visual Feedback

**Development Tools:**
- **Gesture Visualization**: Show gesture paths on screen
- **Timing Indicators**: Visual feedback for gesture timing
- **Success Indicators**: Visual confirmation of gesture completion
- **Error Highlighting**: Visual indication of gesture failures

This gesture implementation provides a robust foundation for automated interaction with Android applications, specifically optimized for FGO while maintaining compatibility and human-like behavior patterns.