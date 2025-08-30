# Lottery Script

The Lottery Script automates lottery events in Fate/Grand Order, including both farming battles and opening lottery boxes.

## Overview

During lottery events, the Lottery Script provides:

- **Automated lottery node farming**: Battle automation specifically optimized for lottery nodes
- **Box opening automation**: Automatically opens lottery boxes and claims rewards
- **Currency management**: Tracks lottery currency and optimizes usage
- **Event detection**: Automatically detects when lottery events are active

## When to Use

The Lottery Script is available during specific events:

- **Christmas events** (annual)
- **Anniversary events** (summer)
- **Special lottery events** (various throughout the year)

The script automatically detects when a lottery event is active and becomes available in the automation menu.

## Setting Up Lottery Automation

### Prerequisites

1. **Event access**: You must have unlocked the lottery event
2. **Lottery currency**: Have sufficient currency to open boxes
3. **Battle configuration**: Set up a battle config for the lottery farming node

### Configuration Steps

1. Navigate to a lottery event farming node in FGO
2. Tap the FGA play button
3. Select "Lottery" from the available scripts
4. Configure your settings:
   - **Battle configuration**: Choose your farming setup
   - **Box opening settings**: Set how many boxes to open
   - **Currency usage**: Configure how much currency to spend per box
   - **Stop conditions**: Set limits for boxes, AP, or time

## Battle Farming Component

### Optimizing for Lottery Nodes

Lottery nodes often have specific characteristics:

- **High HP enemies**: Typically stronger than regular farming nodes
- **Multiple waves**: Usually 3 waves with varying enemy compositions
- **Drop bonuses**: Event CEs provide currency drop bonuses
- **Currency requirements**: Need to balance farming efficiency with box opening

### Recommended Team Setup

**Core Strategy:**
- **Wave 1-2**: AOE servants for efficient clearing
- **Wave 3**: Strong single-target or AOE servant for boss
- **Support**: Use event bonus servants when possible
- **CEs**: Prioritize event CEs for currency bonuses

### Battle Configuration Tips

- **Skill usage**: Configure skills for maximum efficiency
- **NP timing**: Ensure NPs are available for key waves
- **Card selection**: Optimize for both damage and NP gain
- **Support selection**: Use friend supports with event CEs

## Box Opening Component

### Automated Box Opening

The script automatically handles:

1. **Navigation**: Goes to the lottery interface
2. **Box opening**: Opens boxes using accumulated currency
3. **Reward collection**: Claims all rewards from opened boxes
4. **Box progression**: Moves to the next box when current is empty
5. **Inventory management**: Handles full inventory situations

### Box Opening Settings

**Basic Settings:**
- **Boxes to open**: Set maximum number of boxes to open
- **Currency per box**: Amount of currency to spend per box opening session
- **Opening speed**: Adjust timing for box opening animations

**Advanced Settings:**
- **Priority rewards**: Focus on specific rewards first
- **Inventory management**: Handle full inventory scenarios
- **Error handling**: Recovery from unexpected situations

### Currency Management

**Efficient Usage:**
- **Farming cycles**: Balance farming time with box opening time
- **Currency buffer**: Keep some currency for continued farming
- **Event duration**: Plan currency usage over the entire event period

## Event-Specific Considerations

### Christmas Events
- **Lottery boxes**: Typically 10+ boxes with grand prizes
- **Shop currency**: Balance lottery farming with shop item farming
- **Time pressure**: Usually shorter event duration

### Anniversary Events
- **Infinite boxes**: Can open unlimited boxes after reaching a certain point
- **Multiple currencies**: May have separate currencies for different activities
- **Extended duration**: Longer events allow for more methodical farming

### Special Lottery Events
- **Unique mechanics**: May have special rules or requirements
- **Limited boxes**: Some events have a maximum number of boxes
- **Bonus periods**: Take advantage of enhanced drop rates

## Monitoring and Optimization

### Progress Tracking

The script provides real-time information:
- **Boxes opened**: Current progress
- **Currency collected**: Total currency farmed
- **Rewards obtained**: Summary of lottery rewards
- **Time efficiency**: Farming and opening speed metrics

### Performance Optimization

**Farming Efficiency:**
- **Team composition**: Use event bonus servants when possible
- **Battle speed**: Optimize for quick clears
- **AP efficiency**: Balance speed with AP consumption

**Box Opening Efficiency:**
- **Timing settings**: Adjust for smooth box opening
- **Batch processing**: Open multiple boxes in sequence
- **Error recovery**: Handle network issues or game lag

## Common Issues and Solutions

### Script Not Detecting Lottery Event

**Possible Causes:**
- Event not properly unlocked
- Game UI changes during event
- FGA needs updating for new event format

**Solutions:**
- Ensure you've unlocked the lottery event in-game
- Check for FGA updates during events
- Verify you're on the correct event screen

### Box Opening Getting Stuck

**Possible Causes:**
- Inventory full during reward collection
- Network connectivity issues
- Game server lag during events

**Solutions:**
- Clear inventory space before starting
- Use stable internet connection
- Increase timing delays in settings

### Inefficient Currency Usage

**Possible Causes:**
- Poor battle configuration for lottery nodes
- Suboptimal team composition
- Missing event bonus CEs

**Solutions:**
- Optimize battle config for lottery node enemies
- Use servants with event bonuses
- Equip event CEs for currency drops

## Best Practices

### Event Preparation
- **Team optimization**: Prepare teams before the event starts
- **CE preparation**: Level event CEs for maximum bonuses
- **Resource management**: Plan AP and time allocation

### Automation Strategy
- **Gradual approach**: Start with shorter automation sessions
- **Regular monitoring**: Check progress periodically
- **Flexibility**: Adjust settings based on performance

### Event Completion
- **Grand prizes**: Ensure you obtain key rewards from early boxes
- **Shop priorities**: Balance lottery farming with shop item collection
- **Time management**: Plan for event deadline

## Getting Help

For Lottery Script issues:

1. Verify the event is properly unlocked
2. Check that you're using the latest FGA version
3. Review battle configuration for lottery nodes
4. See the [Troubleshooting](../troubleshooting.md) guide
5. Join our [Discord](https://discord.gg/fate-grand-automata) for event-specific support