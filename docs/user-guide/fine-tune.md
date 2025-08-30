# Fine-tuning

Fine-tuning allows you to adjust FGA's timing and behavior to work optimally with your device and network conditions.

## Overview

FGA includes numerous timing and behavior settings that can be adjusted to improve performance, reliability, and compatibility with different devices and network conditions.

## Accessing Fine-tune Settings

1. Open FGA
2. Go to **More options** → **Advanced** → **Fine-tune**
3. Adjust settings based on your device's performance and needs

## Categories of Settings

### Battle Timing

#### Action Delays
- **Skill activation delay**: Time between skill button press and effect
- **Card selection delay**: Pause between card selections
- **Attack animation delay**: Wait time for attack animations
- **Noble Phantasm delay**: Timing for NP activation and animations

#### Turn Management
- **Turn timeout**: Maximum time to wait for turn completion
- **Wave transition delay**: Time between battle waves
- **Battle result delay**: Wait time for battle completion screen

### Screenshot and Recognition

#### Capture Settings
- **Screenshot interval**: How often to take screenshots for analysis
- **Image processing delay**: Time for image analysis completion
- **Pattern matching timeout**: Maximum time for pattern recognition

#### Recognition Thresholds
- **Similarity threshold**: Minimum match confidence for image recognition
- **Multi-pattern detection**: Settings for detecting multiple similar elements
- **Color vs grayscale**: Processing mode preferences

### Support Selection

#### Search Behavior
- **Support refresh limit**: Maximum number of support list refreshes
- **Support search timeout**: Time limit for finding preferred supports
- **Friend list priority**: Timing for friend vs non-friend selection

#### Selection Timing
- **Support tap delay**: Time between support selection and confirmation
- **Class change delay**: Wait time when switching support classes
- **Support confirmation delay**: Pause before confirming support selection

### Network and Connectivity

#### Request Timing
- **Network timeout**: Maximum wait time for network requests
- **Loading screen patience**: How long to wait for screens to load
- **Connection retry delay**: Time between connection retry attempts

#### Error Recovery
- **Automatic retry**: Enable automatic retry on network errors
- **Recovery timeout**: Time to wait before attempting recovery
- **Error detection sensitivity**: How quickly to detect connection issues

### Device Performance

#### Processing Speed
- **CPU usage optimization**: Balance between speed and resource usage
- **Memory management**: Settings for efficient memory usage
- **Battery optimization**: Reduce processing for battery savings

#### Thermal Management
- **Throttling detection**: Detect and respond to device thermal throttling
- **Performance monitoring**: Track device performance during automation
- **Adaptive timing**: Automatically adjust timing based on device performance

## Common Adjustments by Device Type

### High-End Devices
- **Decrease delays**: Faster response times for powerful devices
- **Increase recognition speed**: Higher screenshot frequency
- **Optimize for performance**: Prioritize speed over resource conservation

### Mid-Range Devices
- **Balanced settings**: Default values work well for most scenarios
- **Moderate adjustments**: Small tweaks based on specific issues
- **Monitor performance**: Watch for thermal throttling or lag

### Older/Slower Devices
- **Increase delays**: Give more time for processing and animations
- **Reduce screenshot frequency**: Lower resource usage
- **Conservative timing**: Prioritize reliability over speed

## Network-Specific Adjustments

### Fast, Stable Connection
- **Lower timeouts**: Faster error detection
- **Aggressive retry**: Quick retry on temporary failures
- **Optimized timing**: Faster overall automation

### Slow or Unstable Connection
- **Increased timeouts**: More patience for slow responses
- **Conservative retry**: Avoid overwhelming slow connections
- **Stability focus**: Prioritize completion over speed

### Mobile Data
- **Data conservation**: Minimize unnecessary network activity
- **Extended timeouts**: Account for variable mobile speeds
- **Error tolerance**: Handle connection drops gracefully

## Troubleshooting with Fine-tuning

### FGA Getting Stuck

**Common Causes:**
- Timing too aggressive for device capabilities
- Network timeouts too short
- Recognition thresholds too strict

**Adjustments:**
- Increase relevant delays
- Extend timeout values
- Lower recognition thresholds slightly

### Poor Performance

**Common Causes:**
- Screenshot frequency too high
- Processing delays too conservative
- Resource usage not optimized

**Adjustments:**
- Reduce screenshot interval
- Decrease unnecessary delays
- Enable performance optimizations

### Recognition Issues

**Common Causes:**
- Similarity thresholds too strict
- Processing timing mismatched
- Color detection problems

**Adjustments:**
- Lower similarity requirements
- Adjust image processing delays
- Switch between color and grayscale modes

### Network Problems

**Common Causes:**
- Timeouts too aggressive
- Retry logic too conservative
- Connection handling inadequate

**Adjustments:**
- Increase network timeouts
- Enable automatic retry
- Extend loading screen patience

## Advanced Fine-tuning

### Scenario-Specific Settings
- **Event periods**: Adjust for increased server load during events
- **Different content types**: Optimize settings for farming vs challenging content
- **Time-based adjustments**: Different settings for different times of day

### Performance Monitoring
- **Logging**: Enable detailed logging to identify bottlenecks
- **Statistics tracking**: Monitor success rates and timing performance
- **A/B testing**: Compare different settings to find optimal values

### Automated Optimization
- **Adaptive timing**: Let FGA automatically adjust based on performance
- **Learning algorithms**: Use historical data to optimize settings
- **Dynamic adjustment**: Real-time modification based on current conditions

## Best Practices

### Initial Setup
1. **Start with defaults**: Begin with default settings
2. **Identify issues**: Run automation and note any problems
3. **Gradual adjustment**: Make small changes and test results
4. **Document changes**: Keep track of what works for your setup

### Ongoing Optimization
- **Regular review**: Periodically review and adjust settings
- **Performance monitoring**: Watch for degradation over time
- **Update considerations**: Adjust settings after app or game updates
- **Device changes**: Re-optimize when changing devices

### Backup and Restore
- **Export settings**: Back up your fine-tuned configuration
- **Share configurations**: Exchange settings with users of similar devices
- **Version control**: Keep track of setting changes and their effects

## Getting Help

For fine-tuning assistance:

1. Start with conservative (slower) settings and gradually optimize
2. Check the [Troubleshooting](troubleshooting.md) guide for common issues
3. Search [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues) for device-specific problems
4. Join our [Discord](https://discord.gg/fate-grand-automata) to share settings with similar device users
5. Include your device specifications when asking for help