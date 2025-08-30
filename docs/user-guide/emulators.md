# Running on Emulators

FGA can run on various Android emulators. Here are the tested configurations and setup instructions.

## Supported Emulators

### Nox

On Nox, FGA needs Root to work since MediaProjection doesn't seem to work.

**Setup Instructions:**

1. Make sure you're using an **Android 7** instance of Nox
   - Use [MultiDrive](https://www.bignox.com/blog/how-to-run-multiple-android-instances-with-nox-app-player/) for managing/creating Nox instances
2. Go to `More options` in the FGA app and turn ON `Use Root for Screenshots`
3. Enable `Root` from Nox's settings
   - See [How to root Nox](https://www.bignox.com/blog/how-to-root-nox-app-player/) for detailed instructions

### Bluestacks

These settings are tested and recommended:

**Setup Instructions:**

1. **Install Bluestacks X** (latest version)
2. **Create a proper instance:**
   - Start the Multi-Instance Manager as Admin
   - Create a 64-bit Pie instance
3. **Screenshot settings:**
   - DON'T use root for screenshots (leave disabled in FGA)
4. **Performance mode:**
   - Open Bluestack's settings
   - Go to `Performance` tab
   - Set `Performance mode` to `High Performance`
     - **Note**: For some installations, this breaks Bluestacks as soon as you start the FGA service. If this happens, enable `Compatibility` mode instead
   - Save settings and restart Bluestacks
5. **Install FGA:**
   - Install FGA from the Play Store, not from GitHub
6. **Starting FGA service:**
   - Rotate the screen to portrait mode first before starting the FGA service
   - See [this comment](https://github.com/Fate-Grand-Automata/FGA/issues/967#issuecomment-974652785) for more details

### MEmu

**Setup Instructions:**

1. Enable `Root Mode` in MEmu's `Engine` settings
2. In FGA, enable `Use Root for Screenshots` under `More options` → `Advanced`

### LDPlayer

**Setup Instructions:**

- Works without needing any changes to LDPlayer settings
- No special configuration required

## General Emulator Tips

### Performance Recommendations

- **RAM**: Allocate at least 4GB RAM to your emulator instance
- **CPU**: Use at least 2 CPU cores for better performance
- **Graphics**: Enable hardware acceleration if available
- **Resolution**: Use common Android resolutions (1920x1080, 1280x720)

### Common Issues

#### FGA Play Button Not Appearing

1. Ensure the emulator is in landscape mode
2. Try restarting the accessibility service
3. Check if the emulator has any overlays blocking the button

#### Poor Image Recognition

1. Disable any visual effects or animations in the emulator
2. Ensure the game is running at full resolution
3. Check that there are no screen overlays or filters

#### Performance Issues

1. Close unnecessary background apps on the emulator
2. Increase the allocated RAM and CPU cores
3. Enable hardware acceleration if supported
4. Consider using a simpler emulator if your host system has limited resources

### Root vs Non-Root

| Emulator | Root Required | Notes |
|----------|---------------|-------|
| Nox | ✅ Yes | MediaProjection doesn't work properly |
| Bluestacks | ❌ No | Works with MediaProjection |
| MEmu | ✅ Yes | Better stability with root |
| LDPlayer | ❌ No | Works out of the box |

### Troubleshooting Emulator-Specific Issues

If you're having issues:

1. **Check emulator version**: Make sure you're using a recent version
2. **Try different Android versions**: Some emulators work better with specific Android versions
3. **Reset emulator settings**: Sometimes a fresh emulator instance helps
4. **Hardware acceleration**: Try enabling/disabling hardware acceleration
5. **Compatibility mode**: Try different compatibility modes if available

## Host System Requirements

### Minimum Requirements
- **CPU**: Intel Core i3 or AMD equivalent
- **RAM**: 8GB (4GB for emulator + 4GB for host system)
- **Storage**: 10GB free space
- **Graphics**: Integrated graphics sufficient

### Recommended Requirements
- **CPU**: Intel Core i5/i7 or AMD Ryzen 5/7
- **RAM**: 16GB or more
- **Storage**: SSD with 20GB+ free space
- **Graphics**: Dedicated GPU for better performance

## Getting Help

If you're still having issues with emulators:

1. Check the [Troubleshooting](troubleshooting.md) guide for general issues
2. Search existing [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues) for emulator-specific problems
3. Join our [Discord](https://discord.gg/fate-grand-automata) for community support
4. When reporting issues, include:
   - Emulator name and version
   - Android version in the emulator
   - Host system specifications
   - Specific error messages or behavior