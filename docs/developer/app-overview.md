# Key Points of the App

## Overview

Fate/Grand Automata (FGA) is a native Android automation application specifically designed for Fate/Grand Order (FGO). It automates repetitive farming tasks without requiring root access on Android devices.

## Core Features

### 1. **Auto-Battle Automation**
- Automates farming battles and repetitive tasks in FGO
- Supports multiple battle scenarios and configurations
- Can handle different team compositions and strategies
- Designed for farming, not story progression

### 2. **Non-Intrusive Operation**
- **No root required** on Android 7+ devices
- **No game modification** - works purely through screen observation and input simulation
- **Accessibility-based** - uses Android's built-in accessibility services
- **Safe operation** - simulates human-like interactions

### 3. **Cross-Resolution Support**
- Works on different Android device screen sizes and resolutions
- Automatic scaling and image matching adaptation
- Supports both landscape and portrait orientations
- Game area detection and cropping

## Architecture

### Technology Stack

- **Language**: Kotlin (native Android development)
- **Computer Vision**: OpenCV for image recognition and matching
- **Screenshot Capture**: MediaProjection API for screen capture
- **Input Simulation**: AccessibilityService for clicks and gestures
- **UI Framework**: Android Jetpack Compose for modern UI

### Core Components

#### 1. **Image Recognition Engine**
- Uses OpenCV library for computer vision tasks
- Template matching for UI element detection
- Pattern recognition with configurable similarity thresholds
- Support for both color and grayscale image processing

#### 2. **Screenshot System**
- MediaProjection API for efficient screen capture
- VirtualDisplay for creating screenshot surfaces
- Automatic image format conversion (RGBA to BGR/Grayscale)
- Memory-efficient buffer management

#### 3. **Gesture System**
- AccessibilityService integration for input events
- Platform-specific optimizations (Android 7 vs 8+)
- Configurable timing and duration settings
- Support for clicks, swipes, and complex gestures

#### 4. **Script Management**
- Automatic detection of current game state
- Multiple script types (Battle, Lottery, Friend Gacha, etc.)
- Modular script architecture for extensibility
- Error handling and recovery mechanisms

## Key Capabilities

### 1. **Battle Automation**
- **Servant Selection**: Automatically selects team members based on configured preferences
- **Skill Usage**: Executes servant skills according to pre-defined strategies
- **Card Selection**: Chooses command cards optimally for damage or NP generation
- **Noble Phantasm**: Activates Noble Phantasms when available and beneficial
- **Support Selection**: Picks appropriate support servants from friend list

### 2. **Multi-Script Support**
- **Battle Scripts**: Standard farming battle automation
- **Lottery Scripts**: Automated lottery box opening and management
- **Friend Gacha**: Automated friend point gacha rolling
- **Gift Box**: Automated gift collection and management

### 3. **Configuration System**
- **Flexible Settings**: Extensive customization options for different scenarios
- **Image Templates**: User-configurable servant and CE recognition images
- **Timing Controls**: Adjustable delays and timeouts for different devices
- **Battle Strategies**: Customizable skill rotations and card priorities

### 4. **Safety Features**
- **Human-like Timing**: Variable delays to simulate natural user behavior
- **Error Detection**: Automatic detection of unexpected game states
- **Stop Conditions**: Configurable stopping criteria (AP depletion, item limits, etc.)
- **Manual Override**: Easy pause/stop functionality during execution

## Technical Highlights

### Performance Optimizations
- **Efficient Memory Usage**: Proper OpenCV Mat lifecycle management
- **Screenshot Caching**: Reuse screenshots for multiple image searches
- **Scaled Processing**: Process images at optimal resolution for speed vs accuracy
- **Background Processing**: Non-blocking UI during script execution

### Platform Integration
- **Modern Android APIs**: Uses current Android development practices
- **Material Design**: Follows Android design guidelines
- **Accessibility Compliance**: Proper accessibility service implementation
- **Resource Management**: Efficient handling of system resources

### Extensibility
- **Modular Design**: Easy to add new scripts and features
- **Plugin Architecture**: Separation between core engine and game-specific logic
- **Configuration System**: External configuration without code changes
- **Template System**: User-customizable image recognition patterns

## User Experience

### Setup Process
1. **Simple Installation**: Standard APK installation process
2. **Permission Granting**: Clear guidance for required permissions
3. **Service Activation**: One-click accessibility service enablement
4. **Configuration**: Intuitive UI for setting up automation preferences

### Runtime Experience
- **Floating Action Button**: Unobtrusive overlay button for control
- **Real-time Feedback**: Visual indicators of current automation state
- **Easy Control**: Simple start/stop/pause functionality
- **Status Monitoring**: Clear indication of script progress and status

## Security and Privacy

### Data Handling
- **Local Processing**: All image processing happens on-device
- **No Network Communication**: App doesn't send data to external servers
- **Minimal Permissions**: Only requests necessary Android permissions
- **User Control**: Full user control over automation behavior

### Game Safety
- **Detection Resistance**: Uses human-like timing patterns
- **Conservative Approach**: Focuses on farming rather than competitive advantages
- **Respectful Automation**: Doesn't exploit game vulnerabilities
- **Community Guidelines**: Follows accepted automation practices in the FGO community

This architecture makes FGA a robust, safe, and user-friendly automation solution for FGO players looking to streamline their farming experience.