# Gift Box Automation

FGA can automate the process of collecting items from your Gift Box in Fate/Grand Order.

## Overview

The Gift Box automation feature helps you:

- **Automatically collect** items from your gift box
- **Manage inventory** during collection
- **Handle different item types** appropriately
- **Avoid inventory overflow** with smart collection logic

## When to Use

Gift Box automation is useful when:

- You have many accumulated gifts to collect
- Managing inventory space while collecting
- Collecting specific types of items
- Regular maintenance of your gift box

## How It Works

### Detection and Navigation
FGA automatically detects when you're in the Gift Box screen and can navigate through:
- Gift list scrolling
- Item selection
- Collection confirmation
- Inventory management prompts

### Collection Logic
The automation follows this process:
1. **Scan available gifts** in the current view
2. **Select items** based on your configuration
3. **Handle collection confirmations**
4. **Manage inventory overflow** if it occurs
5. **Continue** until completion or stopping condition

## Configuration Options

### Item Type Selection
Configure which types of items to collect:
- **Materials**: Ascension and skill materials
- **Experience**: Servant and CE experience cards
- **QP**: Quantum Pieces (game currency)
- **Friend Points**: For friend point summoning
- **Special items**: Event items, tickets, etc.

### Collection Behavior
- **Selective collection**: Choose specific item types
- **Bulk collection**: Collect all available items
- **Inventory awareness**: Stop when inventory is full
- **Priority order**: Set priority for different item types

### Safety Settings
- **Collection limits**: Maximum number of items to collect
- **Time limits**: Maximum time for automation session
- **Inventory thresholds**: Stop when inventory reaches certain capacity
- **Error handling**: Behavior when unexpected situations occur

## Inventory Management

### Preventing Overflow
FGA includes several mechanisms to prevent inventory overflow:

#### Smart Detection
- **Monitor inventory status** during collection
- **Detect near-full conditions** before they become problematic
- **Pause collection** when inventory space is limited

#### Handling Full Inventory
When inventory becomes full:
1. **Pause automation** and notify you
2. **Allow manual inventory management**
3. **Resume collection** after space is cleared
4. **Or stop completely** based on your settings

### Item Type Prioritization
Configure collection priority:
- **High priority**: Essential items collected first
- **Medium priority**: Useful items collected when space allows
- **Low priority**: Optional items collected last
- **Skip items**: Items to leave in gift box

## Usage Instructions

### Starting Gift Box Automation

1. **Navigate to Gift Box** in FGO
2. **Tap the FGA play button**
3. **Select Gift Box automation** if available
4. **Configure your collection preferences**
5. **Start automation**

### Monitoring Progress

The automation provides feedback on:
- **Items collected**: Count and types of items
- **Inventory status**: Current space usage
- **Time elapsed**: Duration of collection session
- **Errors or issues**: Any problems encountered

### Manual Intervention

You can intervene during automation:
- **Pause/Resume**: Temporarily stop collection
- **Stop completely**: End the automation session
- **Manual collection**: Handle specific items manually
- **Inventory management**: Clear space and resume

## Common Scenarios

### Daily Gift Collection
- **Regular maintenance**: Collect daily login rewards
- **Quick collection**: Focus on essential items only
- **Time-limited**: Set reasonable time limits for daily use

### Event Gift Management
- **Event items**: Prioritize event-specific materials
- **Bulk collection**: Clear accumulated event rewards
- **Inventory preparation**: Clear space before major collection

### Inventory Cleanup
- **Selective collection**: Choose items that don't consume much space
- **Gradual collection**: Collect in small batches
- **Space management**: Coordinate with other inventory activities

## Best Practices

### Preparation
- **Clear inventory space** before starting large collection sessions
- **Review gift box contents** to plan collection strategy
- **Set appropriate limits** to prevent issues

### During Automation
- **Monitor progress** periodically
- **Be ready to intervene** if inventory fills up
- **Watch for error conditions** that might require attention

### After Collection
- **Review collected items** to ensure expected results
- **Manage inventory** if needed
- **Plan for future collection** sessions

## Troubleshooting

### Automation Not Starting
**Possible Causes:**
- Not on the correct Gift Box screen
- FGA accessibility service not running
- Screen detection issues

**Solutions:**
- Ensure you're on the main Gift Box screen
- Restart FGA accessibility service
- Check game area detection

### Collection Getting Stuck
**Possible Causes:**
- Inventory full dialog not properly handled
- Network lag causing timing issues
- Unexpected game UI changes

**Solutions:**
- Clear inventory space manually
- Increase timing delays in fine-tune settings
- Check for game updates

### Incorrect Item Selection
**Possible Causes:**
- Configuration settings incorrect
- Image recognition issues
- Game UI changes affecting detection

**Solutions:**
- Review and adjust collection settings
- Update FGA if available
- Report issues with specific item types

## Limitations

### Current Limitations
- **Cannot manage inventory automatically**: Manual inventory management required when full
- **Limited to visible items**: Only processes items currently shown on screen
- **Network dependent**: Requires stable connection for reliable operation

### Workarounds
- **Pre-clear inventory**: Ensure adequate space before starting
- **Batch processing**: Collect in smaller groups to avoid overflow
- **Manual oversight**: Monitor automation for best results

## Getting Help

For Gift Box automation issues:

1. Ensure you're on the correct Gift Box screen in FGO
2. Check that you have adequate inventory space
3. Review the [Troubleshooting](troubleshooting.md) guide
4. Search [GitHub Issues](https://github.com/Fate-Grand-Automata/FGA/issues) for gift box problems
5. Join our [Discord](https://discord.gg/fate-grand-automata) for community support