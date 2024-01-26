- [Introduction](#introduction)
- [Video Introduction](#video-introduction)
- [Battle Configs](#battle-configs)
  - [Step 1 - Enter the Battle Configs menu](#step-1---enter-the-battle-configs-menu)
  - [Step 2 - Create new script with the floating action button.](#step-2---create-new-script-with-the-floating-action-button)
  - [Step 3 - Give your script a name](#step-3---give-your-script-a-name)
  - [Step 4 - Edit the command](#step-4---edit-the-command)
- [Commands](#commands)
  - [Party Mapping](#party-mapping)
  - [Screen Mapping](#screen-mapping)
  - [Skills mapping](#skills-mapping)
  - [Targetable](#targetable)
  - [Special Skills](#special-skills)
    - [Kukulkan](#kukulkan)
    - [Emiya](#emiya)
    - [Space Ishtar](#space-ishtar)
    - [Melusine/Ptolemy](#melusineptolemy)
      - [Warning about Melusine option.](#warning-about-melusine-option)
  - [Enemy targetable](#enemy-targetable)
  - [Noble Phantasm (NP) Order](#noble-phantasm-np-order)
    - [Cards before NP](#cards-before-np)
  - [Master skills](#master-skills)
  - [Master skills - Plugsuit](#master-skills---plugsuit)
  - [Next turn in the same wave, and Next wave](#next-turn-in-the-same-wave-and-next-wave)
    - [Next Wave](#next-wave)
    - [Next Turn in the same wave](#next-turn-in-the-same-wave)
    - [Wave and Turn Indicator](#wave-and-turn-indicator)
    - [Raid Battle](#raid-battle)
  - [Example of FGA Battle Config setup and used](#example-of-fga-battle-config-setup-and-used)
- [Additional Battle Config (optional)](#additional-battle-config-optional)
  - [Materials](#materials)
  - [Spam](#spam)
  - [Server](#server)
  - [Party Selection](#party-selection)
  - [Card Priority (optional)](#card-priority-optional)
    - [Card Priority explanation](#card-priority-explanation)
      - [Symbols used in Card Priority](#symbols-used-in-card-priority)
    - [Servant Priority](#servant-priority)
    - [Brave Chain](#brave-chain)
    - [Rearrange Cards feature](#rearrange-cards-feature)
  - [Support Selection](#support-selection)
    - [Class Selection](#class-selection)
    - [Support Selection Options](#support-selection-options)
    - [Preferred Selection](#preferred-selection)
      - [Preferred Selection - Preferred Servant](#preferred-selection---preferred-servant)
      - [Preferred Servant - Preferred CE](#preferred-servant---preferred-ce)
      - [Preferred Servant - Preferred Friends](#preferred-servant---preferred-friends)
      - [Final Look](#final-look)
      - [Fallback](#fallback)

# Introduction

First and foremost, if you are new to FGA - you have come to the right place.

This battle section is strictly for FGA Battle Configuration.

# Video Introduction

Please be warned it is a bit outdated, but it should give you a general idea of how to setup FGA for yourself.

<a href="https://youtube.com/watch?v=JOwupZ4W8AQ&feature=youtu.be" target="_blank"> FGA Tutorial - Build 1437 </a>

# Battle Configs

Assuming you have watched the video introduction, this section will go more in-depth tutorial on setting up a battle config.

This in-depth tutorial will cover on creating scripts that will use 'Skills', 'NP', 'Command Cards' and 'Pick Supports'.

## Step 1 - Enter the Battle Configs menu

Find the below icon and label and select it.

![Battle Configs image](https://cdn.discordapp.com/attachments/1127606706420068372/1127616861782147154/20230709_100604.jpg "Battle Configs")

## Step 2 - Create new script with the floating action button.

Create new script by tapping this icon

![Battle Configs - Create New Script](https://cdn.discordapp.com/attachments/1127606706420068372/1127617402792837150/20230709_100808.jpg "Battle Configs - Create New Script")

## Step 3 - Give your script a name

![Battle Configs - Name Script](https://cdn.discordapp.com/attachments/1127606706420068372/1127618080370073690/20230709_101031.jpg "Battle Configs - Name Script")

You can also add a special note (optional) to your script.

The note can be anything like which servants are apart the team you use this script on and so on

## Step 4 - Edit the command

This is where you'll create your commands that FGA will use in battle.

![Battle Configs - Command](https://cdn.discordapp.com/attachments/1127606706420068372/1127619924370665552/20230709_101515.jpg "Command")

# Commands

## Party Mapping

When creating a battle script, you have to remember that servant positions are important.

Each servant in your party will be given a number from 1 to 6, first to last.
![Battle Configs - Servant Position in FGO ](https://cdn.discordapp.com/attachments/1127606706420068372/1127622383767916625/Screenshot_20200116-225324_Video_Player.jpg "Servant Position in FGO ")
![Battle Configs - Servant Position Reference in FGA](https://cdn.discordapp.com/attachments/1127606706420068372/1127622384229306449/20230709_102808.jpg "Servant Position Reference in FGA")

## Screen Mapping

For your reference, look at the images below on how to map with FGA.

![Battle Configs - FGO to FGA Command mapping](https://cdn.discordapp.com/attachments/1127606706420068372/1127622994605396038/20200429_183015.jpg "FGO to FGA Command mapping")
![Battle Configs - FGA Command Interface](https://i.imgur.com/6N9OHcx.png "FGA Command Interface")

## Skills mapping

When creating scripts 'Skills' and 'NP', you create by basing on your servant positions with each skill is represented by a letter on the command
creator.

![Battle Configs - FGO to FGA Command mapping | Servant skills](https://cdn.discordapp.com/attachments/1127606706420068372/1127625668465807371/20230709_103946.jpg "FGO to FGA Command mapping | Servant skills")
![Battle Configs - FGO to FGA Command mapping | Servant skills](https://cdn.discordapp.com/attachments/1127606706420068372/1127625668746813460/20230709_104022.jpg "FGA Command | Servant skills")

## Targetable

Skills that are targetable will use the servants position number to apply the skill to them

![Battle Configs - FGA Command | Servant skills - targetable](https://cdn.discordapp.com/attachments/1127606706420068372/1127627494963556362/Screenshot_20230709-104541_FGA_CI.jpg "FGA Command | Servant skills - targetable")
![Battle Configs - FGO to FGA Command mapping | Servant skills - targetable](https://cdn.discordapp.com/attachments/1127606706420068372/1127627495294894121/20230709_104707.jpg "FGO to FGA Command mapping | Servant skills - targetable")

## Special Skills

Due to the nature of some skills, they will have a different/additional command mapping to accommodate them.

### Kukulkan

[First Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#First_Skill)
and [Third Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#Third_Skill) is a self targetable skill that has option to use critical stars for
more effects.

Use `Option 1` if you don't plan to use critical stars.

![Kukulkan use option 1](https://i.imgur.com/rwUpGpH.gif)

Use `Option 2` if you plan to use critical stars.

![Kukulkan use option 2](https://i.imgur.com/d7AYeJ3.gif)

![Kuku s1/s3](https://i.imgur.com/JwD0rAe.gif)

---

[Second Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#Second_Skill) is a targetable skill that has option to use critical stars for more
effects.

Use `Option 1` if you don't plan to use critical stars.

![Kukulkan use option 1](https://i.imgur.com/TvOxKOR.gif)

Use `Option 2` if you plan to use critical stars.

![Kukulkan use option 2](https://i.imgur.com/DVQ65aN.gif)

![kuku s2](https://i.imgur.com/b7Pdy4E.gif)

---

### Emiya

Emiya's [Third Skill](https://fategrandorder.fandom.com/wiki/EMIYA#Third_Skill) can select an NP card type after its second upgrade.

Only use the `"Emiya"` option if you've fully upgraded the third skill.

![Emiya's Third Skill Max Upgrade](https://i.imgur.com/ZKH1qSV.png)

or the support you'll borrow have it fully upgraded.

![Emiya's Support Third Skill Max Upgrade](https://i.imgur.com/sFI8IQx.png)

Note: FGA doesn't have the capability to detect if the skill is fully upgraded or not, but you can use preferred friends to ensure that you get
the right support.

Using Arts option

![Emiya use Arts](https://i.imgur.com/hlknb1L.gif)

Using Buster option

![Emiya use Buster](https://i.imgur.com/1oDpuav.gif)

Battle

![Emiya](https://i.imgur.com/sYxXXO2.gif)

---

### Space Ishtar

For convenience's sake, Space Ishtar's [Second Skill](https://fategrandorder.fandom.com/wiki/Space_Ishtar#Second_Skill) has her option also explicitly
shown.

![Space Ishtar Option](https://i.imgur.com/CoNfanB.gif)

![Ishtar](https://i.imgur.com/CuBYrDT.gif)

---

### Melusine/Ptolemy

Melusine's [Third Skill at Ascension 1 and 2](https://fategrandorder.fandom.com/wiki/M%C3%A9lusine#Third_Skill) and
Ptolemy's [Third Skill](https://fategrandorder.fandom.com/wiki/Ptolemaios#Third_Skill) both have capability to alter the NP type after the skill use.
This causes a long transition animation and this option will help mitigate that problem.

![Melusine/Ptolemy Option](https://i.imgur.com/Uij44bW.gif)

![Melusine option](https://i.imgur.com/hX2mGjA.gif)

#### Warning about Melusine option.

After using Melusine's third skill at ascension 1 and 2, any succeeding use of third skill must use the `normal third skill` option.

While Ptolemy will keep using the `Melusine` option. As Ptolemy keeps changing back and forth between Ascension 1 and Ascension 3 with each use of his
third skill.

---

## Enemy targetable

Like your servants, enemies also have a number and can be targeted too.

![Battle Configs - FGO to FGA Command mapping | Enemies - targetable](https://cdn.discordapp.com/attachments/1127606706420068372/1127628553186447443/20230709_105236.jpg "FGO to FGA Command mapping | Enemies - targetable")![Battle Configs - FGA Command | Enemies - targetable](https://cdn.discordapp.com/attachments/1127606706420068372/1127628553484247100/20230709_105306.jpg "FGA Command | Enemies - targetable")

Like in FGO, you can only select one enemy at a time.

![Battle Configs - FGA Command | Enemies - targetable](https://i.imgur.com/174JOiI.gif "FGA Command | Enemies - targetable")

## Noble Phantasm (NP) Order

Noble Phantasm (NP) use your servants position.

**Note:** NP can be used in any order.
Keep in mind, they will be __*used in the order you choose*__ them.

![Battle Configs - FGO to FGA Command mapping | Noble Phantasm](https://cdn.discordapp.com/attachments/1127606706420068372/1127629873721135235/20230709_105703.jpg "FGO to FGA Command mapping | Noble Phantasm")
![Battle Configs - FGA Command | Noble Phantasm](https://cdn.discordapp.com/attachments/1127606706420068372/1127629874018914344/20230709_105818.jpg "FGA Command | Noble Phantasm")

### Cards before NP

If you ever need to use cards before NP, you can select the following options.

![Cards before NP](https://i.imgur.com/XTrJTZT.png)

## Master skills

Master skills is handled the same way for all 'Mystic Codes' with an exception to the plugsuit.

![Battle Configs - FGO to FGA Command mapping | Mystic Codes](https://cdn.discordapp.com/attachments/1127606706420068372/1127632299337453689/20230709_110424.jpg "FGO to FGA Command mapping | Mystic Codes")

![Battle Configs - FGA Command | Mystic Code](https://cdn.discordapp.com/attachments/1127606706420068372/1127632299568136342/20230709_110454.jpg "FGA Command | Mystic Code")

## Master skills - Plugsuit

The `l` command should not be used when using the plugsuit mystic code. Instead, the plugsuit order change command should be used with

![Battle Configs - FGA Command | Mystic Code - Plugsuit](https://i.imgur.com/LQVSDkA.png "FGA Command | Mystic Code - Plugsuit")

This will trigger the plugsuit order change menu.

![Order Change](https://i.imgur.com/BY5izMc.png)

## Next turn in the same wave, and Next wave

This step will cover both wave and turn options.

![Battle Configs - FGA Command | Wave and Turn options](https://cdn.discordapp.com/attachments/1127606706420068372/1127635393924300943/20230709_111920.jpg "FGA Command | Wave and Turn options")

### Next Wave

A wave or battle that's displayed in the top right corner

![Battle Configs - FGA Command | Wave](https://cdn.discordapp.com/attachments/1127606706420068372/1127637782043566090/20230709_112240.jpg)

After settings your skills or NPs for a wave you can switch to the next wave by pressing this "Next Wave" button.

![Battle Configs - FGA Command | Next Wave Symbol](https://cdn.discordapp.com/attachments/1127606706420068372/1127642577462628433/20230709_111816.jpg "Next Wave Symbol")

Take note of the symbol above. This will indicate that you've skipped to the next wave.

By wave skipping, you're telling FGA that you don't want do anything else until you're on the next wave.

**Be warned** that if you use the next wave button then any skill you use after that will only activate during the next wave so make sure you check
over your script to see if you have made any mistakes.

___

### Next Turn in the same wave

![Battle Configs - FGA Command | Next Turn Symbol](https://cdn.discordapp.com/attachments/1127606706420068372/1127644007313461249/20230709_112155.jpg "Next Turn Symbol")

Turns are indicated by this symbol

![Battle Configs - FGA Command | Next Turn Symbol](https://cdn.discordapp.com/attachments/1127606706420068372/1127644007611248680/20230709_111757.jpg "Next Turn Symbol")

Turn skipping can't be use to switch to the next wave and vice versa.

---

### Wave and Turn Indicator

With the recent update as of FGA [Build 2382](https://github.com/Fate-Grand-Automata/FGA/releases/tag/2382). You'll be able now to track the current
wave and turn

![Main menu](https://i.imgur.com/yvcme5d.png)

![Attack Menu](https://i.imgur.com/x9Jzjxg.png)

---

### Raid Battle

Raid Battles more often happens in the same wave.

![Raid](https://i.imgur.com/cCjdCIM.jpeg)

If that happens, you must select the `Next turn in the same wave` option.

![Raid Option](https://i.imgur.com/oBUJcqJ.png)

___

## Example of FGA Battle Config setup and used

I recommend opening the video below for reference before continuing this FGA Battle Config guide.
<a href="https://youtu.be/ahY4tFEpUSg" target="_blank"> FGA Config setup and used video </a>

Example command from video:

![FGA Battle Configs example from video - Command](https://cdn.discordapp.com/attachments/1127606706420068372/1127661024938557610/20230709_124941.png "FGA Battle Configs example from video - Command")

# Additional Battle Config (optional)

## Materials

Materials is use in combination with another setting in the pop up menu that will be covered later for when you want to farm a specific material.

![Battle Configs - Material](https://cdn.discordapp.com/attachments/1127606706420068372/1127664742601850931/20230709_130905.jpg "Battle Configs - Material")

## Spam

How the spam option works

[https://github.com/Fate-Grand-Automata/FGA/issues/510](https://github.com/Fate-Grand-Automata/FGA/issues/510 "https://github.com/Fate-Grand-Automata/FGA/issues/510")

## Server

If you play in multiple servers you can make your scripts only show up only for that specific server in the pop up menu when you're in-game.

![Battle Configs - Server Selection](https://cdn.discordapp.com/attachments/1127606706420068372/1127690674284798032/20230709_145949.jpg "Battle Configs - Server Selection")

## Party Selection

You can assign one of the party slots to your scripts so that whenever you start FGA, it will check if your last use party is on the right slot and
switch to the right one if it's not.

![Battle Configs - Party Selection](https://cdn.discordapp.com/attachments/1127606706420068372/1127690720606691338/20230709_145513.jpg "Battle Configs - Party Selection")

## Card Priority (optional)

Card Priority is an optional feature.
Please note, you do not have to set them everytime you're making a new script.

![Battle Configs - Card Priority](https://cdn.discordapp.com/attachments/1127606706420068372/1127694004822225007/20230709_151143.jpg "Battle Configs - Card Priority")

___

### Card Priority explanation

Card Priority is used to tell FGA how to handle face cards.

With cards in the high will have priority usage over cards on the low end

![Battle Configs - Card Priority Interface](https://cdn.discordapp.com/attachments/1127606706420068372/1127696989459591208/20230709_152209.jpg)

#### Symbols used in Card Priority

- WB = Weak Buster
- WA = Weak Art
- WQ = Weak Quick
- B - Buster
- A - Art
- Q - Quick
- RB = Resist Buster
- RA = Resist Art
- RQ = Resist Quick

Cards can be changed around for different use case eg:

- For Buster cards priority:

  ![Battle Configs - Card Priority | Buster](https://cdn.discordapp.com/attachments/1127606706420068372/1127705166628540427/20230709_153937.jpg "Battle Configs - Card Priority | Buster")

- For charging NP with face cards (Arts cards priority):

  ![Battle Configs - Card Priority | Art](https://cdn.discordapp.com/attachments/1127606706420068372/1127713422881063013/20230709_154002.jpg "Battle Configs - Card Priority | Art")

- For stars (Quick cards priority):

  ![Battle Configs - Card Priority | Quick](https://cdn.discordapp.com/attachments/1127606706420068372/1127713500937076927/20230709_154016.jpg "Battle Configs - Card Priority | Quick")

If you want to have different card priority for different waves, then you can add more waves and rearrange the card priority to your liking.

Otherwise, the default will be used throughout all waves, so if you want the same priority, you will only need to just setup the wave 1 priority
option OR leave it as it is.

Hence, there is no reason to add more if you're not gonna use it for something different.
![Battle Configs - Card Priority | Default](https://cdn.discordapp.com/attachments/1127606706420068372/1127718272234696774/20230709_164455.jpg "Battle Configs - Card Priority | Default")
___

### Servant Priority

Servant priority takes higher priority over card priority.

In order to activate the servant priority you must turn it on.

![Servant priority on](https://i.imgur.com/KSXE36s.png)

Then you can now select the servant you want to prioritize.

![Servant priority](https://i.imgur.com/fz9BBtX.png)

Follow the positioning on the [Party Mapping](#party-mapping)

___

### Brave Chain

There are only 3 options for brace chains unless you use Servant Priority.

![Battle Configs - Card Priority | Brave Chain options](https://cdn.discordapp.com/attachments/1127606706420068372/1127719287168180314/20230709_165137.jpg "Battle Configs - Card Priority | Brave Chain options")
___

### Rearrange Cards feature

FGA reads face cards from left to right, starting from the right corner to the left.

![Battle Configs - Card Priority | Default face card reading](https://cdn.discordapp.com/attachments/1127606706420068372/1127726348337696808/Untitled_design.png "Battle Configs - Card Priority | Default face card reading")

Turning on 'Rearrange cards' will flip the order of how FGA reads cards from left > right to right < left

![Battle Configs - Card Priority | Rearrange Cards](https://cdn.discordapp.com/attachments/1127606706420068372/1127720604972368032/20230709_165504.jpg "Battle Configs - Card Priority | Rearrange Cards")
![Battle Configs - Card Priority | Rearrange CardsUsed](https://cdn.discordapp.com/attachments/1127606706420068372/1127726957342232646/Untitled_design_1.png "Battle Configs - Card Priority | Rearrange Cards Used")

Then, FGA will pick the first card that matches the settings in card priority in that order.

___

## Support Selection

### Class Selection

If you do not select any class, FGA will pick support from the current class that is showing in the screen.

![Battle Configs - Support Selection | Class Selection](https://i.imgur.com/fM3HW6p.png "Battle Configs - Support Selection | Class Selection")

### Support Selection Options

There is 3 ways to select support.

![Battle Configs - Support Selection](https://i.imgur.com/ZAJsjDK.png "Battle Configs - Support Selection")

1. First

   FGA will pick the first support that shows up in the support selection screen.

2. Manual

   FGA will exit and you must manually pick the support. After picking up the support, Run the FGA again.

3. Preferred

   FGA will pick the support that matches the settings in support selection in that order.

### Preferred Selection

If you want to specify which support you want to use, you can use the preferred selection.

![Battle Configs - Support Selection | Preferred Selection](https://i.imgur.com/VcKxqmu.png "Battle Configs - Support Selection | Preferred Selection")

In order to set the preferred settings, you must click on the preferred selection option.

![Battle Configs - Support Selection | Preferred Selection](https://i.imgur.com/baddVZi.png "Battle Configs - Support Selection | Preferred Selection")

Then, you can set the preferred settings.

#### Preferred Selection - Preferred Servant

Click on the preferred servants to show the popup dialog. Then you can select the preferred serant that you want.

![Servants](https://i.imgur.com/G2aheeG.png)

Then you can select if you want the preferred servant to be `max ascended`

And the what skills to be `max level`

![Servant Settings](https://i.imgur.com/NRpwsuV.png)

Note: We can only reliably detect if the skill level is max level or not. We can't detect the current skill level.

#### Preferred Servant - Preferred CE

Click on the preferred CE to show the popup dialog. Then you can select the preferred CE that you want.

![CE](https://i.imgur.com/017VHsd.png)

Then you can select if you want the preferred CE to be `max limit broken`

![CE Settings](https://i.imgur.com/SimCcz4.png)

#### Preferred Servant - Preferred Friends

Enable this option if you want to use your friends as preferred support.

If there are no images in the `support` -> `friends` directory, This will be the screen that will show up. You need to manually add via the Support Image Maker.

![Preferred Friends](https://i.imgur.com/GAq5wIK.png)

Once you have added the images, you can now select the preferred friends.

![Preferred Friends](https://i.imgur.com/yJGbnsn.png)

#### Final Look

![Preferred Selection](https://i.imgur.com/nWQiBFC.png)

Note: You can use only one(CE only) or any of the combinations of the preferred settings.

#### Fallback

The script would continue checking the next support if the preferred support is not found, until the specific settings in the fine tuning is met.
Afterwards, it will then pick the action depending on what is set in the fallback option.

![Fallback](https://i.imgur.com/1z9dP1t.png)

In order to know the limits of searching for the preferred support.

Go to More Options -> Advanced -> Fine-Tune -> Support.

![Battle Configs - Support Selection | Preferred Selection | Fine-Tune](https://i.imgur.com/Ewh6o4r.png "Battle Configs - Support Selection | Preferred Selection | Fine-Tune")