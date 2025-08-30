# Getting Started with FGA

This guide will help you set up Fate/Grand Automata for the first time.

## Installation

### From Google Play Store (Recommended)

1. Install FGA from the [Google Play Store](https://play.google.com/store/apps/details?id=io.github.fate_grand_automata)
2. This version allows you to use the Accessibility Service feature without restrictions
3. Automatic updates are available through the Play Store

### From GitHub Releases

1. Download the latest APK from [GitHub Releases](https://github.com/Fate-Grand-Automata/FGA/releases)
2. Enable "Install from unknown sources" in your Android settings
3. Install the downloaded APK file

!!! note "Package Change Notice"
    Starting with June 2023, the app is published under the package `io.github.fate_grand_automata`. If you're upgrading from the old `com.mathewsachin.fategrandautomata` version, you'll need to transfer your settings manually.

## Initial Setup

### 1. Enable Accessibility Service

1. Open the FGA app
2. Grant the required permissions when prompted
3. Enable the Accessibility Service in Android Settings
4. The app will guide you through this process

!!! warning "Battery Optimization"
    Make sure to disable battery optimization for FGA to prevent it from being killed by the system. This is crucial for stable operation.

### 2. Configure Game Area Detection

The app needs to properly detect the game area on your screen:

1. Start Fate/Grand Order
2. Rotate your device to landscape mode
3. The FGA play button should appear in the bottom-left corner
4. If it doesn't appear, see [Game Area Detection](game-area-detection.md) for troubleshooting

### 3. Create Your First Battle Config

1. In FGA, go to the main screen
2. Tap the "+" button to create a new battle configuration
3. Configure your preferred settings:
   - **Team composition**
   - **Skill usage patterns**
   - **Card selection preferences**
   - **Noble Phantasm priorities**

### 4. Test Your Setup

1. Go to a simple farming quest in FGO
2. Tap the FGA play button
3. Select your battle configuration
4. Let FGA run a single battle to verify everything works correctly

## Basic Usage

### Starting Automation

1. Navigate to the quest you want to farm in FGO
2. Make sure your device is in landscape mode
3. Tap the FGA play button (bottom-left corner)
4. Select your battle configuration
5. Configure run settings (number of runs, stop conditions, etc.)
6. Tap "Start" to begin automation

### Stopping Automation

- **From notification**: Use the FGA notification to stop
- **Play button**: Tap the play button again and select "Stop"
- **Force stop**: Force-close the FGA app if needed

## Next Steps

Once you have FGA working:

- **[Configure Scripts](scripts/index.md)** - Set up different automation scripts
- **[Fine-tune Settings](fine-tune.md)** - Optimize timing and behavior
- **[Troubleshooting](troubleshooting.md)** - Solve common issues

## Need Help?

If you encounter issues during setup:

1. Check the [Troubleshooting](troubleshooting.md) guide
2. Ensure you're following the [Game Area Detection](game-area-detection.md) guidelines
3. Join our [Discord](https://discord.gg/fate-grand-automata) for community support
4. Report bugs on [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues)