# Battle Script

The Battle Script is the core automation feature of FGA, designed to handle regular farming quests, events, and daily missions automatically.

## Overview

The Battle Script automates the entire battle process from start to finish:

- **Skill management**: Automatically uses servant skills based on your configuration
- **Card selection**: Chooses the optimal cards for each turn
- **Noble Phantasm usage**: Manages NP timing and targeting
- **Battle completion**: Handles battle results and continues to the next run

## Setting Up Battle Configurations

### Creating a New Configuration

1. Open FGA and go to the main screen
2. Tap the "+" button to create a new battle configuration
3. Give your configuration a descriptive name
4. Configure the settings for your specific farming setup

### Basic Configuration Options

#### Team Setup
- **Party selection**: Choose which party slot to use (1-10)
- **Servant positions**: Configure which servants are in each position
- **Combat uniform**: Select the appropriate Mystic Code

#### Skill Usage
- **Skill priorities**: Set which skills to use and when
- **Target selection**: Configure skill targets for buffs/debuffs
- **Skill timing**: Set turn-based or HP-based skill triggers

#### Card Selection
- **Card priorities**: Set preferences for Buster, Arts, Quick cards
- **Chain priorities**: Configure for Buster chains, Arts chains, etc.
- **Servant priorities**: Prioritize cards from specific servants

#### Noble Phantasm Settings
- **NP priorities**: Set which servant's NP to use first
- **NP timing**: Configure when to use NPs (turn-based, charge-based)
- **NP targeting**: Set targeting for single-target NPs

### Advanced Configuration

#### Conditional Logic
- **Turn-based conditions**: Different behavior for different turns
- **HP-based triggers**: Activate skills based on enemy/ally HP
- **Charge-based logic**: Use skills/NPs based on NP charge levels

#### Support Selection
- **Support servant**: Configure automatic support selection
- **Support priorities**: Set preferences for support servant types
- **Friend vs non-friend**: Prioritize friend supports

#### Stop Conditions
- **Run limits**: Set maximum number of runs
- **AP limits**: Stop when AP falls below threshold
- **Item drops**: Stop after specific CE drops or rare materials
- **Time limits**: Stop automation after specified time

## Using the Battle Script

### Starting Automation

1. Navigate to the quest you want to farm in FGO
2. Make sure you're using the correct party setup
3. Tap the FGA play button
4. Select your battle configuration
5. Configure run-specific settings (number of runs, stop conditions)
6. Tap "Start" to begin automation

### Monitoring Progress

- **FGA notification**: Shows current progress and status
- **Battle counter**: Tracks completed runs
- **AP monitoring**: Shows remaining AP
- **Error detection**: Alerts for unexpected issues

### Manual Intervention

You can manually intervene during automation:

- **Pause/Resume**: Temporarily pause automation
- **Stop**: End automation at any time
- **Configuration changes**: Some settings can be modified during runs

## Optimization Tips

### For Efficiency
- **Minimize skill usage**: Only use necessary skills to save time
- **Optimize card selection**: Focus on damage output over elaborate chains
- **Support selection**: Use supports that complement your strategy

### For Reliability
- **Test configurations**: Always test with a few manual runs first
- **Conservative timing**: Use slightly longer delays for stability
- **Error handling**: Configure appropriate stop conditions

### For Different Content Types

#### Daily Quests
- Simple configurations with minimal skill usage
- Focus on speed over optimization
- Use AOE servants for wave clearing

#### Event Farming
- Configure for event-specific mechanics
- Adjust for event CEs and bonuses
- Consider time-limited nature of events

#### Challenge Quests
- More complex skill rotations
- Conditional logic for different phases
- Manual oversight recommended

## Common Issues and Solutions

### Battle Script Gets Stuck

**Possible causes:**
- Game lag causing timing issues
- Unexpected battle events (critical hits, skill effects)
- Configuration errors

**Solutions:**
- Increase delays in fine-tuning settings
- Simplify battle configuration
- Check for game updates or changes

### Wrong Skills Being Used

**Possible causes:**
- Skill confirmation setting mismatch
- Configuration errors
- Game UI changes

**Solutions:**
- Check FGO's skill confirmation setting
- Enable/disable skill confirmation in FGA to match
- Review and test skill configuration

### Poor Card Selection

**Possible causes:**
- Card priority configuration issues
- Game lag affecting detection
- Face card detection problems

**Solutions:**
- Review card priority settings
- Enable face card detection for better accuracy
- Increase delays for card selection

### Support Selection Issues

**Possible causes:**
- Support list changes
- Friend support unavailability
- Support image recognition problems

**Solutions:**
- Create custom support images
- Configure fallback support options
- Use Support Image Maker for better detection

## Best Practices

### Configuration Management
- **Descriptive names**: Use clear names for different farming setups
- **Regular backups**: Export configurations regularly
- **Version control**: Keep track of configuration changes

### Testing and Validation
- **Test runs**: Always do test runs before long automation sessions
- **Performance monitoring**: Track success rates and adjust accordingly
- **Regular updates**: Update configurations for game changes

### Resource Management
- **AP efficiency**: Optimize for AP usage and regeneration
- **Time management**: Balance automation time with other activities
- **Device care**: Monitor device temperature and performance

## Advanced Techniques

### Multi-Phase Battles
- Configure different strategies for different battle phases
- Use conditional logic for adaptive behavior
- Account for enemy behavior changes

### Optimization for Specific Servants
- Tailor configurations for servant-specific mechanics
- Optimize skill usage for servant synergies
- Account for servant-specific animations and timing

### Event-Specific Strategies
- Adapt configurations for event mechanics
- Optimize for event currency and materials
- Account for event-specific enemy types

## Getting Help

For Battle Script issues:

1. Check configuration settings carefully
2. Test with simpler content first
3. Review the [Troubleshooting](../troubleshooting.md) guide
4. Search [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues)
5. Join our [Discord](https://discord.gg/fate-grand-automata) for community support