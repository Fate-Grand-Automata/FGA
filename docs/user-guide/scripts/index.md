# Scripts Overview

FGA includes several automation scripts for different types of content in Fate/Grand Order. Each script is designed for specific scenarios and automatically detects when it can be used.

## Available Scripts

### [Battle Script](battle.md)
The main automation script for farming quests and battles.

- **Use case**: Regular farming quests, events, dailies
- **Features**: Full battle automation, skill management, card selection
- **Requirements**: Battle configuration setup

### [Lottery Script](lottery.md)
Specialized script for lottery events with box opening automation.

- **Use case**: Lottery events (Christmas, Anniversary, etc.)
- **Features**: Automatic box opening, prize claiming, battle automation
- **Requirements**: Lottery event must be active

### [Support Image Maker](support-image-maker.md)
Tool for creating custom support servant images for better detection.

- **Use case**: Improving support servant selection accuracy
- **Features**: Custom image creation, support detection optimization
- **Requirements**: Access to support selection screen

## How Script Detection Works

FGA automatically detects which scripts are available based on the current screen:

1. **Screen Analysis**: FGA analyzes the current FGO screen
2. **Context Detection**: Identifies the current game state (quest, lottery, support, etc.)
3. **Script Availability**: Shows only relevant scripts in the automation menu
4. **Dynamic Updates**: Updates available scripts as you navigate through the game

## Using Scripts

### Starting a Script

1. Navigate to the appropriate screen in FGO
2. Tap the FGA play button (bottom-left corner)
3. Select the script you want to use
4. Configure script-specific settings
5. Tap "Start" to begin automation

### Script Configuration

Each script has its own configuration options:

- **Battle Script**: Team setup, skill usage, card priorities
- **Lottery Script**: Box opening preferences, battle settings
- **Support Image Maker**: Image capture and optimization settings

### Stopping Scripts

Scripts can be stopped in several ways:

- **Completion**: Script finishes its programmed task
- **Manual stop**: Use the FGA notification or play button
- **Error conditions**: Script stops on unexpected errors
- **Stop conditions**: Configured conditions like AP exhaustion or item drops

## Script Configuration Tips

### Battle Script Optimization
- Configure skill usage based on your servants
- Set appropriate card selection priorities
- Test with simple quests first

### Lottery Script Efficiency
- Pre-configure battle settings for lottery nodes
- Set reasonable box opening limits
- Monitor AP usage during events

### Support Image Maker Best Practices
- Create images in good lighting conditions
- Capture full servant portraits
- Test recognition accuracy after creation

## Common Script Issues

### Script Not Appearing
- Check if you're on the correct screen
- Ensure FGA accessibility service is running
- Verify game area detection is working

### Script Getting Stuck
- Review configuration settings
- Check for game updates that might affect recognition
- Increase delays if your device is slow

### Poor Performance
- Optimize script configuration for your device
- Close unnecessary background apps
- Check for thermal throttling on your device

## Advanced Script Features

### Conditional Logic
- Stop after specific item drops
- AP threshold management
- Time-based automation limits

### Multi-Script Workflows
- Chain different scripts together
- Switch between farming and lottery
- Adaptive script selection

### Performance Monitoring
- Track automation statistics
- Monitor success rates
- Optimize based on performance data

## Getting Help with Scripts

For script-specific issues:

1. Check the individual script documentation
2. Review the [Troubleshooting](../troubleshooting.md) guide
3. Search [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues) for script problems
4. Join our [Discord](https://discord.gg/fate-grand-automata) for community support

## Contributing Script Improvements

Scripts are continuously improved based on user feedback:

- Report bugs and issues on GitHub
- Suggest new features or improvements
- Share configuration tips with the community
- Contribute to script development if you have programming experience