# Image Matching Implementation

## Overview

The image matching system in Fate/Grand Automata is built on OpenCV (Open Source Computer Vision Library) and provides robust pattern recognition capabilities for game automation. The system captures screenshots and matches them against template images to detect UI elements and game states.

## Core Components

### 1. Screenshot Capture System

#### MediaProjectionScreenshotService
The primary screenshot capture mechanism uses Android's MediaProjection API:

```kotlin
class MediaProjectionScreenshotService(
    private val mediaProjection: MediaProjection,
    private val imageSize: Size,
    private val screenDensity: Int,
    private val storageProvider: StorageProvider,
    private val colorManager: ColorManager
) : ScreenshotService
```

**Key Features:**
- **VirtualDisplay Creation**: Creates a virtual display surface for screenshot capture
- **Memory Efficient**: Uses direct buffer access to avoid unnecessary memory copies  
- **Format Conversion**: Automatically converts between RGBA, BGR, and Grayscale formats
- **Resource Management**: Proper cleanup of OpenCV Mat objects and Android resources

**Screenshot Process:**
1. **ImageReader Setup**: Creates an ImageReader with RGBA_8888 format
2. **VirtualDisplay**: MediaProjection creates virtual display targeting ImageReader surface
3. **Buffer Access**: Direct access to pixel buffer from captured image
4. **Mat Creation**: Creates OpenCV Mat from buffer data without copying
5. **Format Conversion**: Converts to appropriate format (color/grayscale) based on settings

### 2. Pattern Recognition Engine

#### DroidCvPattern Class
The core pattern matching implementation:

```kotlin
class DroidCvPattern(
    val mat: Mat, 
    ownsMat: Boolean = true, 
    override var tag: String = ""
) : Pattern
```

**Template Matching Algorithm:**
- Uses OpenCV's `matchTemplate()` function with `TM_CCOEFF_NORMED` method
- Provides normalized correlation coefficient matching
- Supports similarity thresholds for flexible matching

**Matching Process:**
```kotlin
override fun findMatches(template: Pattern, similarity: Double) = sequence {
    val result = match(template)
    
    result?.use {
        while (true) {
            val minMaxLocResult = Core.minMaxLoc(it)
            val score = minMaxLocResult.maxVal
            
            if (score >= similarity) {
                val loc = minMaxLocResult.maxLoc
                val region = Region(
                    loc.x.roundToInt(),
                    loc.y.roundToInt(), 
                    template.width,
                    template.height
                )
                
                val match = Match(region, score)
                yield(match)
                
                // Eliminate nearby matches using flood fill
                result.floodFill(loc, 0.3, 0.0)
            } else {
                break
            }
        }
    }
}
```

### 3. Image Processing Pipeline

#### Screenshot Manager
Coordinates screenshot capture and caching:

```kotlin
class ScreenshotManager @Inject constructor(
    private val gameAreaManager: GameAreaManager,
    private val screenshotService: ScreenshotService,
    private val scale: Scale
) : AutoCloseable
```

**Key Features:**
- **Screenshot Caching**: `usePreviousSnap` flag enables reusing screenshots
- **Game Area Cropping**: Automatically crops to relevant game area
- **Scaling**: Applies appropriate scaling transformations
- **Memory Management**: Proper cleanup of cached patterns

**Processing Pipeline:**
1. **Raw Screenshot**: Capture full screen using MediaProjection
2. **Game Area Detection**: Identify and crop to game boundaries  
3. **Scaling**: Apply scale transformations for resolution independence
4. **Caching**: Store processed image for reuse during matching operations

#### Scale and Resolution Handling

The scaling system handles different device resolutions:

```kotlin
class RealScale @Inject constructor(
    private val gameAreaManager: GameAreaManager
) : Scale {
    override val screenToImage: Double?
    override val scriptToScreen: Double
    override val scriptToImage: Double
}
```

**Scaling Types:**
- **Screen to Image**: Ratio between actual screen and template image resolution
- **Script to Screen**: Ratio between script coordinates and screen coordinates  
- **Script to Image**: Combined ratio for direct script-to-template coordinate mapping

### 4. Image Matcher Integration

#### RealImageMatcher
High-level interface for pattern matching operations:

```kotlin
class RealImageMatcher @Inject constructor(
    private val exitManager: ExitManager,
    private val screenshotManager: ScreenshotManager,
    private val platformImpl: PlatformImpl,
    private val wait: Waiter,
    private val highlight: Highlighter,
    private val transform: Transformer
) : ImageMatcher
```

**Main Functions:**

**Pattern Finding:**
```kotlin
override fun findAll(
    region: Region, 
    pattern: Pattern, 
    similarity: Double?
): Sequence<Match> {
    return screenshotManager.getScreenshot()
        .crop(transform.toImage(region))
        .findMatches(pattern, similarity ?: 0.8)
        .map { match ->
            val adjustedRegion = transform.toScreen(
                match.region + transform.toImage(region).location
            )
            Match(adjustedRegion, match.score)
        }
}
```

**Existence Checking:**
```kotlin
override fun exists(
    items: Map<Pattern, Region>,
    timeout: Duration,
    similarity: Double?,
    requireAll: Boolean
): Boolean {
    val imageCheck = {
        if (requireAll) {
            items.all { (image, region) ->
                region.existsNow(image, similarity)
            }
        } else {
            items.any { (image, region) ->
                region.existsNow(image, similarity)
            }
        }
    }
    
    return checkConditionLoop({ imageCheck() }, timeout)
}
```

## Advanced Features

### 1. Color vs Grayscale Processing

The system supports both color and grayscale image processing:

```kotlin
override fun takeScreenshot(): Pattern {
    screenshotIntoBuffer()
    
    return if (colorManager.isColor) {
        Imgproc.cvtColor(bufferMat, colorMat, Imgproc.COLOR_RGBA2BGR)
        colorPattern
    } else {
        Imgproc.cvtColor(bufferMat, grayscaleMat, Imgproc.COLOR_RGBA2GRAY)
        grayscalePattern
    }
}
```

**Benefits:**
- **Grayscale**: Faster processing, more lighting-tolerant
- **Color**: More precise matching for color-dependent UI elements

### 2. Similarity Thresholding

Configurable similarity thresholds allow fine-tuning match sensitivity:

- **High Threshold (0.9+)**: Exact matches, less tolerance for variations
- **Medium Threshold (0.7-0.9)**: Balanced matching, handles minor variations
- **Low Threshold (0.5-0.7)**: Loose matching, more tolerant of differences

### 3. Multi-Pattern Matching

Support for matching multiple patterns simultaneously:

```kotlin
// Check if any pattern exists in region
val patterns = mapOf(
    buttonPattern to buttonRegion,
    iconPattern to iconRegion
)

val anyExists = imageMatcher.exists(patterns, timeout, requireAll = false)
```

### 4. Flood Fill Optimization

Prevents detection of nearby duplicate matches:
- After finding a match, flood fill is applied to eliminate surrounding high-correlation areas
- Ensures only distinct pattern instances are detected
- Improves performance by avoiding redundant matches

## Performance Optimizations

### 1. Memory Management
- **Mat Lifecycle**: Proper creation and disposal of OpenCV Mat objects
- **Buffer Reuse**: Reusing screenshot buffers when possible
- **Reference Counting**: Tracking Mat ownership to prevent memory leaks

### 2. Screenshot Caching
- **Single Screenshot**: Use same screenshot for multiple pattern searches
- **Snap System**: `useSameSnapIn()` function for temporary caching
- **Cache Invalidation**: Automatic cleanup when new screenshot needed

### 3. Resolution Optimization
- **Scaled Processing**: Process images at optimal resolution for speed vs accuracy
- **ROI Processing**: Only process relevant regions of interest
- **Early Termination**: Stop processing when sufficient matches found

## Error Handling

### 1. Robustness Features
- **Bounds Checking**: Verify template fits within screenshot boundaries
- **Resource Cleanup**: Automatic cleanup on errors or exceptions
- **Fallback Strategies**: Alternative approaches when primary matching fails

### 2. Debugging Support
- **Match Highlighting**: Visual feedback for successful/failed matches
- **Logging Integration**: Comprehensive logging using Timber
- **Score Reporting**: Detailed similarity scores for analysis

## Integration Points

### 1. Script Integration
The image matching system integrates with automation scripts through:
- **AutomataApi**: High-level interface for script developers
- **Region-based Operations**: Scripts specify regions for pattern matching
- **Event-driven Architecture**: Callbacks for match events

### 2. UI Integration  
- **Live Preview**: Real-time screenshot preview during setup
- **Template Management**: UI for creating and managing pattern templates
- **Debug Visualization**: Visual feedback for match results

This image matching implementation provides a robust foundation for game automation, balancing accuracy, performance, and reliability while maintaining flexibility for different use cases and device configurations.