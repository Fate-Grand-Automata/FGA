# Game Area Detection

For FGA to work properly, it needs to correctly detect the game area on your screen. This guide explains how game area detection works and how to troubleshoot detection issues.

## How Game Area Detection Works

FGA uses image recognition to identify the boundaries of the Fate/Grand Order game on your screen. It looks for specific UI elements and patterns that are consistent across different devices and screen sizes.

### Detection Process

1. **Screenshot Capture**: FGA takes a screenshot of your entire screen
2. **Pattern Matching**: Searches for known FGO UI elements using OpenCV
3. **Boundary Calculation**: Determines the exact game area coordinates
4. **Validation**: Verifies the detected area makes sense (aspect ratio, size, etc.)

## Ensuring Proper Detection

### Screen Orientation
- **Always use landscape mode** when running FGA
- Portrait mode is not supported for automation
- The FGA play button only appears in landscape mode

### Game Display Settings

Make sure FGO is displayed correctly:

1. **Full screen**: The game should fill the available screen area
2. **No overlays**: Remove or disable any screen overlays, filters, or blue light filters
3. **Standard UI**: Don't use any FGO UI scaling options that might distort elements
4. **No zoom**: Ensure system-level zoom/magnification is disabled

### Common Display Issues

#### Navigation Bars and Status Bars
- **Android navigation bar**: Should be hidden or account for in detection
- **Status bar**: Usually handled automatically by the detection algorithm
- **Notches**: Modern phones with notches are generally supported

#### Screen Filters and Overlays
These can interfere with detection:
- Blue light filters
- Screen dimming overlays
- Accessibility overlays
- Gaming mode overlays
- Screen recording overlays (except FGA's built-in recorder)

#### Custom Launchers and Themes
- Some custom Android launchers may interfere
- System themes that change UI colors can cause issues
- Always test with default system appearance first

## Troubleshooting Detection Issues

### Play Button Not Appearing

If the FGA play button doesn't appear:

1. **Check orientation**: Ensure you're in landscape mode
2. **Restart services**: 
   - Force-stop FGA
   - Disable and re-enable Accessibility Service
   - Restart FGA
3. **Clear overlays**: Disable any screen overlays or filters
4. **Check game state**: Make sure FGO is running and visible

### Incorrect Game Area

If FGA detects the wrong area:

1. **Check for interference**: Look for UI elements that might confuse detection
2. **Restart detection**: Force-stop and restart FGA
3. **Clean game display**: Ensure no overlays or unusual UI elements
4. **Report the issue**: If detection consistently fails, report it with screenshots

### Performance Impact

Poor detection can cause:
- **Slow automation**: FGA takes longer to find elements
- **Incorrect clicks**: Touches in wrong locations
- **Failed script execution**: Scripts may get stuck or fail

## Device-Specific Considerations

### Samsung Devices
- **Game Booster**: Disable Samsung Game Booster/Game Tools
- **Edge Panels**: May interfere with detection
- **One UI features**: Some One UI customizations can affect detection

### LG Devices
- **Game Tools**: Turn OFF LG Game Tools
- **Alert blocking**: Game Tools can prevent FGA dialogs from appearing

### Motorola Devices
- **Gaming mode**: Disable Motorola's gaming mode features
- **Display enhancements**: Turn off any Motorola-specific display features

### Emulators
- **Resolution**: Use standard Android resolutions (1920x1080, 1280x720)
- **DPI scaling**: Avoid non-standard DPI settings
- **Performance mode**: Use appropriate performance settings for your host system

## Advanced Troubleshooting

### Debug Information

To get debug information about detection:

1. Enable debug options in FGA settings
2. Check the detection logs in the app
3. Use the screen recording feature to capture detection behavior

### Manual Verification

You can manually verify detection:

1. Take a screenshot while FGO is running
2. Check if the game area is clearly defined
3. Look for any visual interference or distortion
4. Compare with working setups on similar devices

### Reporting Detection Issues

When reporting detection problems:

1. **Device information**: Model, Android version, screen resolution
2. **Screenshots**: Show the FGO screen where detection fails
3. **FGA version**: Include the FGA version you're using
4. **Reproduction steps**: Detailed steps to reproduce the issue
5. **Logs**: Include any error logs or debug information

## Best Practices

### For Reliable Detection
- Use standard Android settings and appearance
- Keep FGO updated to the latest version
- Avoid system modifications that change display behavior
- Test detection before starting automation sessions

### For Optimal Performance
- Close unnecessary apps that might create overlays
- Ensure adequate device performance (avoid thermal throttling)
- Use stable internet connection for consistent game loading
- Keep FGA updated to benefit from detection improvements

## Getting Help

If you're still experiencing detection issues:

1. Check the [Troubleshooting](troubleshooting.md) guide
2. Search [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues) for similar problems
3. Join our [Discord](https://discord.gg/fate-grand-automata) community
4. Provide detailed information when asking for help