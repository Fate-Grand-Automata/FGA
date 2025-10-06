
- [Introduction](#introduction)
- [Video Introduction](#video-introduction)
- [Battle Configs](#battle-configs)
  - [Step 1 - Enter the Battle Configs menu](#step-1---enter-the-battle-configs-menu)
  - [Step 2 - Create new script with the floating action button.](#step-2---create-new-script-with-the-floating-action-button)
  - [Step 3 - Give your script a name](#step-3---give-your-script-a-name)
  - [Step 4 - Edit the command](#step-4---edit-the-command)
- [Commands](#commands)
  - [Party Mapping](#party-mapping)
  - [Skills mapping](#skills-mapping)
  - [Targeting](#targeting)
  - [Special Skills](#special-skills)
    - [Kukulkan/UDK-Barghest](#kukulkanudk-barghest)
      - [Kukulkan](#kukulkan)
      - [UDK-Barghest](#udk-barghest)
    - [Emiya](#emiyabb-dubai)
    - [Space Ishtar](#space-ishtar)
    - [Mélusine/Ptolemaios](#mélusineptolemaios)
      - [Warning about Mélusine option.](#warning-about-the-mélusine-option)
    - [Soujuurou/Charlotte](#soujuuroucharlottehakunovan-gogh-miner)
  - [Enemy Targeting](#enemy-targeting)
  - [Noble Phantasm (NP) Order](#noble-phantasm-np-order)
    - [Cards before NP](#cards-before-np)
  - [Master skills](#master-skills)
  - [Master skills - Plugsuit](#master-skills---plugsuit)
  - [Next turn in the same wave, and Next wave](#next-turn-in-the-same-wave-and-next-wave)
    - [Next Wave](#next-wave)
    - [Next Turn in the same wave](#next-turn-in-the-same-wave)
    - [Wave and Turn Indicator](#wave-and-turn-indicator)
    - [Raid Battle](#raid-battle)
- [Additional Battle Config (optional)](#additional-battle-config-optional)
  - [Materials](#materials)
  - [Spam](#spam)
  - [Server](#server)
  - [Party Selection](#party-selection)
  - [Card Priority (optional)](#card-priority-optional)
    - [Card Priority explanation](#card-priority-explanation)
      - [Symbols used in Card Priority](#symbols-used-in-card-priority)
    - [Servant Priority](#servant-priority)
    - [Chain Priority](#chain-priority)
      - [Interaction with other priorities](#interaction-with-other-priorities)
      - [Mighty Chain](#mighty-chain)
      - [Buster / Arts / Quick Chain](#buster--arts--quick-chain)
      - [Avoid Chain](#avoid-chain)
    - [Rearrange Cards](#rearrange-cards)
    - [Brave Chain](#brave-chain)
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

![Battle Configs image](https://i.imgur.com/gHvukdq.jpeg "Battle Configs")

## Step 2 - Create new script with the floating action button.

Create new script by tapping this icon

![Battle Configs - Create New Script](https://i.imgur.com/9zUDLWf.jpeg "Battle Configs - Create New Script")

## Step 3 - Give your script a name

![Battle Configs - Name Script](https://i.imgur.com/O108gg6.jpeg "Battle Configs - Name Script")

You can also add a special note (optional) to your script.

The note can be anything like which servants are apart the team you use this script on and so on

## Step 4 - Edit the command

This is where you'll create your commands that FGA will use in battle.

![Battle Configs - Command](https://i.imgur.com/2pzGUNp.jpeg "Command")

# Commands

## Party Mapping

When creating a battle script, you have to remember that servant positions are important.

Each servant in your party will be given a number from 1 to 6, first to last.  
![Battle Configs - Servant Position in FGO ](https://i.imgur.com/w14U3rm.jpeg "Servant Position in FGO ")  
![Battle Configs - Servant Position Reference in FGA](https://i.imgur.com/v0Bllrk.jpeg "Servant Position Reference in FGA")

## Skills mapping

When creating scripts 'Skills' and 'NP', you create by basing on your servant positions with each skill is represented by a letter on the command  
creator.

![Battle Configs - FGO to FGA Command mapping | Servant skills](https://i.imgur.com/qsYHZBi.jpeg "FGO to FGA Command mapping | Servant skills")  
![Battle Configs - FGO to FGA Command mapping | Servant skills](https://i.imgur.com/O3WlnEK.jpeg "FGA Command | Servant skills")

## Targeting

Skills that are targetable will use the servants position number to apply the skill to them

![Battle Configs - FGA Command | Servant skills - targetable](https://i.imgur.com/CSwxJbX.jpeg "FGA Command | Servant skills - targetable")  
![Battle Configs - FGO to FGA Command mapping | Servant skills - targetable](https://i.imgur.com/48SRqqX.jpeg "FGO to FGA Command mapping | Servant skills - targetable")

## Special Skills

Due to the nature of some skills, they will have a different/additional command mapping to accommodate them.

After clicking on a skill in the command builder, FGA will show buttons at the bottom to deal with the special skills.
![Special Skill Buttons](https://github.com/user-attachments/assets/2a0c7d82-44a7-4add-9c7d-60cb49b56977)

### Kukulkan/UDK-Barghest
#### Kukulkan
Kukulkan's [First Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#First_Skill) and [Third Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#Third_Skill) are self-targeting skills which can gain optional effects by paying crit stars.

Use `Option 1` if you don't want to use critical stars.

![Kukulkan use option 1](https://github.com/user-attachments/assets/5b25221d-98d9-49a4-ade6-9c59ed6ae4f5)

Use `Option 2` if you want to use critical stars.

![Kukulkan use option 2](https://github.com/user-attachments/assets/51091fab-ac7f-4bd3-86d4-2c27087d4405)

You can click on the Kukulkan button below to replace the button labels with more helpful ones.

Note: You don't have to always click it when using certain servants, this is only for visual purposes.

![Updating Button labels for choices 2 option](https://github.com/user-attachments/assets/53f7d44b-9af8-4a49-a9ea-525e00eff1da)

**Battle**  
![Kuku s1/s3](https://i.imgur.com/JwD0rAe.gif)
  
---  
The [Second Skill](https://fategrandorder.fandom.com/wiki/Kukulkan#Second_Skill) is a targetable skill which can also gain an additional effect by paying crit stars.

Use `Option 1` if you don't want to use critical stars or `Option 2` if you want to. Afterwards, choose a Servant to target.

**Battle**  
![kuku s2](https://i.imgur.com/b7Pdy4E.gif)

#### UDK-Barghest
UDK-Barghest's [Third Skill](https://fategrandorder.fandom.com/wiki/UDK-Barghest#Third_Skill) can optionally turn her NP from an AoE to a Single Target Noble Phantasm.

To choose between AoE and Single Target, first click `Choices (2)` and then click `Option 1` for AoE or `Option 2` for Single Target.

You can click the `UDK-Barghest` button at the bottom to change the button descriptions to more fitting ones.
![Barghest s3](https://github.com/user-attachments/assets/d8cfd999-d6b8-493e-b967-ec9751db1477)

### Emiya/BB Dubai
Emiya and BB Dubai can change their NP to 1 of 2 variants with their skills. After clicking on a 3rd skill in the command builder, `NP Type (2)` will appear at the bottom and needs to be picked for those Servants.

Only use the `"NP Type(2)"` option if you're using an Emiya with a fully upgraded third skill.  
![Emiya's Third Skill Max Upgrade](https://i.imgur.com/ZKH1qSV.png)  
![Emiya's Support Third Skill Max Upgrade](https://i.imgur.com/sFI8IQx.png)  
Note: FGA doesn't have the capability to detect if the skill is fully upgraded or not, but you can use preferred friends to ensure that you get  the right support.

After clicking, `NP Type (2)`, you'll see 2 Options. Click on the buttons at the bottom to see the effects of each Option for Emiya and BB Dubai.

Note: You don't have to always click it when using certain servants, this is only for visual purposes.

![Emiya and BB Dubai Options](https://github.com/user-attachments/assets/3c608461-9809-4c8d-851c-9c8bdcb5e0f1)

**Battle**  
![Emiya](https://i.imgur.com/sYxXXO2.gif)
### Space Ishtar

Space Ishtar's [Second Skill](https://fategrandorder.fandom.com/wiki/Space_Ishtar#Second_Skill) can also change her NP type, but she has 3 options to choose from: Quick, Arts and Buster

After clicking on a 2nd Servant Skill, `NP Type (3)` will appear at the bottom, which will lead you to a new screen where you can select one of 3 options. You can check what would the skill at that option do by pressing the buttons below.

Note: You don't have to always click it when using certain servants, this is only for visual purposes.

![Updating Button labels for three targets option](https://github.com/user-attachments/assets/43997d9f-154b-4c7c-89e7-a2d405c154df)

**Battle**  
![Ishtar](https://i.imgur.com/CuBYrDT.gif)

### Mélusine/Ptolemaios
Mélusine's [Third Skill at Ascension 1 and 2](https://fategrandorder.fandom.com/wiki/M%C3%A9lusine#Third_Skill) and Ptolemaios's [Third Skill](https://fategrandorder.fandom.com/wiki/Ptolemaios#Third_Skill) change the Servant's Ascension  
forms in the middle of the battle.

Use the `Transform` option to both account for the long animation and for the changed face cards. Otherwise, Servant Priority will be broken for the rest of the battle.
![Mélusine/Ptolemaios Option](https://github.com/user-attachments/assets/2b224ce2-48a6-4a21-9753-c328b207c7a2)

Battle

![Mélusine option](https://i.imgur.com/hX2mGjA.gif)

#### Warning about the Mélusine option.
After using Mélusine's third skill at ascension 1 and 2, any succeeding use of third skill  
should use the normal third skill. So if Mélusine is in the 1st position, use `c`.

If you're already starting the battle with Mélusine in Ascension 3, there's no need to use  
the `Transform` option since she won't change forms.

With Ptolemaios you will need to keep using the `Transform` option as he changes forms on every  
use of his third skill.

### Soujuurou/Charlotte/Hakuno/Van Gogh Miner
Some Servants have special skills with 3 choices, which requires clicking the `Choices (3)` button in FGA.

- Soujuurou's [3rd skill](https://fategrandorder.fandom.com/wiki/Shizuki_S%C5%8Dj%C5%ABr%C5%8D#Third_Skill) can change the command cards after one turn
- Charlotte's [Upgraded 3rd skill](https://fategrandorder.fandom.com/wiki/Charlotte_Corday#Third_Skill) can have different effects depending on the choice you choose.
- Hakuno's [3rd skill](https://fategrandorder.fandom.com/wiki/Kishinami_Hakuno_(Female)#Third_Skill) can increase damage for 3 turns.
- Van Gogh Miner's [1st skill](https://fategrandorder.fandom.com/wiki/Van_Gogh_(Miner)#First_Skill) can increase card effectiveness for 3 turns.

![Choice Three Option](https://github.com/user-attachments/assets/3eedd53f-d7ab-40ea-9e08-bc7dd1d55033)

You can check what would the skill at that option do by pressing the buttons below.

Note: You don't have to always click it when using certain servants, this is only for visual purposes.
![Updating Button labels for choice three option slot 1](https://github.com/user-attachments/assets/3514c367-2170-4063-9f15-7fe923b479a4)

![Updating Button labels for choice three option slot 3](https://github.com/user-attachments/assets/efff0a9c-ad4c-40e8-af36-40139a8269ca)

**Battle**  
![Soujuurou](https://i.imgur.com/Dg3k6jM.gif)
## Enemy Targeting

Like your servants, enemies also have a number and can be targeted too.

![Battle Configs - FGO to FGA Command mapping | Enemy Targeting](https://i.imgur.com/bWmlZxc.jpeg "FGO to FGA Command mapping | Enemy Targeting")![Battle Configs - FGA Command | Enemy Targeting](https://i.imgur.com/obg3W9d.jpeg "FGA Command | Enemy Targeting")

Like in FGO, you can only select one enemy at a time.

![Battle Configs - FGA Command | Enemy Targeting](https://i.imgur.com/174JOiI.gif "FGA Command | Enemy Targeting")

## Noble Phantasm (NP) Order

Noble Phantasm (NP) use your servants position.

**Note:** NP can be used in any order.  
Keep in mind, they will be __*used in the order you choose*__ them.

![Battle Configs - FGO to FGA Command mapping | Noble Phantasm](https://i.imgur.com/QqAdwQO.jpeg "FGO to FGA Command mapping | Noble Phantasm")  
![Battle Configs - FGA Command | Noble Phantasm](https://i.imgur.com/wGC1mDx.jpeg "FGA Command | Noble Phantasm")

### Cards before NP

If you ever need to use cards before NP, you can select the following options.

![Cards before NP](https://i.imgur.com/XTrJTZT.png)

## Master skills

Master skills is handled the same way for all 'Mystic Codes' with an exception to the Plugsuit.

![Battle Configs - FGO to FGA Command mapping | Mystic Codes](https://i.imgur.com/2FTn2rj.jpeg "FGO to FGA Command mapping | Mystic Codes")

![Battle Configs - FGA Command | Mystic Code](https://i.imgur.com/c97pPEU.jpeg "FGA Command | Mystic Code")

## Master skills - Plugsuit

The `l` command should not be used when using the Plugsuit mystic code. Instead, the Plugsuit order change command should be used with

![Battle Configs - FGA Command | Mystic Code - Plugsuit](https://i.imgur.com/LQVSDkA.png "FGA Command | Mystic Code - Plugsuit")

This will trigger the Plugsuit order change menu.

![Order Change](https://i.imgur.com/BY5izMc.png)

## Next turn in the same wave, and Next wave

This step will cover both wave and turn options.

![Battle Configs - FGA Command | Wave and Turn options](https://i.imgur.com/L1PhUKr.jpeg "FGA Command | Wave and Turn options")

### Next Wave

A wave or battle that's displayed in the top right corner

![Battle Configs - FGA Command | Wave](https://i.imgur.com/2AKqyb4.jpeg)

After settings your skills or NPs for a wave you can switch to the next wave by pressing this "Next Wave" button.

![Battle Configs - FGA Command | Next Wave Symbol](https://i.imgur.com/1oxfjxB.jpeg "Next Wave Symbol")

Take note of the symbol above. This will indicate that you've skipped to the next wave.

By wave skipping, you're telling FGA that you don't want do anything else until you're on the next wave.

**Be warned** that if you use the next wave button then any skill you use after that will only activate during the next wave so make sure you  
double-check your script to see if you have made any mistakes.

___  

### Next Turn in the same wave

![Battle Configs - FGA Command | Next Turn Symbol](https://i.imgur.com/oLEXWYG.jpeg "Next Turn Symbol")

Turns are indicated by this symbol

![Battle Configs - FGA Command | Next Turn Symbol](https://i.imgur.com/YuEz0sl.jpeg "Next Turn Symbol")
  
---  

### Wave and Turn Indicator

With the recent update as of FGA [Build 2382](https://github.com/Fate-Grand-Automata/FGA/releases/tag/2382). You're now able to track the current  
wave and turn in your command.

![Main menu](https://i.imgur.com/yvcme5d.png)

![Attack Menu](https://i.imgur.com/x9Jzjxg.png)
  
---  

### Raid Battle

Raid Battles can sometimes only have 1 wave.

![Raid](https://i.imgur.com/cCjdCIM.jpeg)

In those cases, you must select the `Next turn in the same wave` option.

![Raid Option](https://i.imgur.com/oBUJcqJ.png)

___  

# Additional Battle Config (optional)

## Materials

Materials can be used when you want to farm a specific material.

![Battle Configs - Material](https://i.imgur.com/ctteami.jpeg "Battle Configs - Material")

![Material Selection](https://i.imgur.com/2IQKvSj.png)

Any Selected Material will be shown at the top of the list.

![Selected Material](https://i.imgur.com/FGyjJng.png)

After selecting the material and pressing `OK`, you'll see a summary of the first 3 selected materials.

![Material Summary](https://i.imgur.com/LDrLxre.png)

## Spam

How the spam option works

[https://github.com/Fate-Grand-Automata/FGA/issues/510](https://github.com/Fate-Grand-Automata/FGA/issues/510 "https://github.com/Fate-Grand-Automata/FGA/issues/510")

## Server

If you play in multiple servers you can make your scripts only show up only for that specific server in the pop up menu when you're in-game.

![Battle Configs - Server Selection](https://i.imgur.com/ihEg1WS.jpeg "Battle Configs - Server Selection")

## Party Selection

You can assign one of the party slots to your scripts so that whenever you start FGA, it will check if your last use party is on the right slot and  
switch to the right one if it's not.

![Battle Configs - Party Selection](https://i.imgur.com/ACFI8kc.jpeg "Battle Configs - Party Selection")

## Card Priority (optional)

Card Priority is an optional feature.  
Please note, you do not have to set them everytime you're making a new script.

![Battle Configs - Card Priority](https://i.imgur.com/XbdSqhZ.jpeg "Battle Configs - Card Priority")

___  

### Card Priority explanation

Card Priority is used to tell FGA how to handle face cards.

With cards in the high will have priority usage over cards on the low end

![Battle Configs - Card Priority Interface](https://i.imgur.com/VGszBHI.jpeg)

#### Symbols used in Card Priority

- $${\color{red}WB = Weak Buster}$$
- $${\color{blue}WA = Weak Art}$$
- $${\color{green}WQ = Weak Quick}$$
- $${\color{red}B - Buster}$$
- $${\color{blue}A - Art}$$
- $${\color{green}Q - Quick}$$
- $${\color{red}RB = Resist Buster}$$
- $${\color{blue}RA = Resist Art}$$
- $${\color{green}RQ = Resist Quick}$$

Cards can be changed around for different use case eg:

- For Buster cards priority:

  ![Battle Configs - Card Priority | Buster](https://i.imgur.com/0I1BRzp.jpeg "Battle Configs - Card Priority | Buster")

- For charging NP with face cards (Arts cards priority):

  ![Battle Configs - Card Priority | Art](https://i.imgur.com/c0IRnTr.jpeg "Battle Configs - Card Priority | Art")

- For stars (Quick cards priority):

  ![Battle Configs - Card Priority | Quick](https://i.imgur.com/2a483et.jpeg "Battle Configs - Card Priority | Quick")

If you want to have different card priority for different waves, then you can add more waves and rearrange the card priority to your liking.

Otherwise, the default will be used throughout all waves, so if you want the same priority, you will only need to just setup the wave 1 priority  
option OR leave it as it is.

Hence, there is no reason to add more if you're not gonna use it for something different.  
![Battle Configs - Card Priority | Default](https://i.imgur.com/N6WfLVB.jpeg "Battle Configs - Card Priority | Default")
___  

### Servant Priority

Servant priority takes higher priority over card priority.

In order to activate the servant priority you must turn it on.

![Servant priority on](https://i.imgur.com/KSXE36s.png)

Then you can now select the servant you want to prioritize.

![Servant priority](https://i.imgur.com/fz9BBtX.png)

Follow the positioning on the [Party Mapping](#party-mapping)

___

### Chain Priority

![Chain priority on](../img/attack/chain-priority-toggle.png)

Chain Priority is a new feature that allows for attempts at `Mighty`, $${\color{red}Buster}$$, $${\color{blue}Arts}$$ or $${\color{green}Quick}$$ Chains, based on the order that the chains are chosen in. There is an additional option called "Avoid", which when used attempts to avoid any of the other chains. 

| Sample orders                                                                       | Name                            | Description                                                                                                                                                                                                                          | 
|:------------------------------------------------------------------------------------|:--------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| ![Chain priority options default](../img/attack/chain-priority-options-default.png) | Default order                   | The default order used when Chain Priority is turned on. This disables Avoid Chain.                                                                                                                                                  | 
| ![Chain priority options buster](../img/attack/chain-priority-options-buster.png)   | $${\color{red}Buster}$$ focused | This order disables all chains except $${\color{red}Buster}$$ (highest priority) and `Mighty` (second priority). Failure to execute any of these chains will result in no other chain being (actively) used.                         |
| ![Chain priority options arts](../img/attack/chain-priority-options-arts.png)       | $${\color{blue}Arts}$$ only     | Only $${\color{blue}Arts}$$ chains will be attempted. All other chain checks are skipped.                                                                                                                                            | 
| ![Chain priority options avoid](../img/attack/chain-priority-options-avoid.png)     | "Avoid" only                    | All chains will be avoided, unless it is absolutely impossible (e.g. all cards are $${\color{green}Quick}$$ cards).                                                                                                                  | 
| ![Chain priority options none](../img/attack/chain-priority-options-none.png)       | None                            | This completely skips all checks. It is almost the same as turning Chain Priority off, except that it uses Chain Priority's [Brave Chain](#brave-chain) handling instead of the default (which allows usage of the 'Always' option). |

The way Chain Priority works is that it checks for Chains in the order that is shown (from left to right). If it finds a chain, then it will use that chain immediately.

E.g. If Chain Priority is on and `Default order` is used, it will attempt to check for a `Mighty` Chain first. If there is no valid `Mighty` Chain, then it will attempt to check for a $${\color{red}Buster}$$ Chain. If there is no valid $${\color{red}Buster}$$ Chain, it will attempt to check for an $${\color{blue}Arts}$$ Chain and so on and so forth. If an $${\color{blue}Arts}$$ Chain was found, it will skip checking for a $${\color{green}Quick}$$ Chain using the cards and will immediately use the $${\color{blue}Arts}$$ Chain. However, if all options fail to find any usable Chain, it will fallback to Servant Priority + Card Priority to determine the cards used.   

|                         Flowchart of entire system                         | 
|:--------------------------------------------------------------------------:|
| ![Attack Priority flow chart](../img/attack/attack-priority-flowchart.png) |

#### Interaction with other priorities

In general, the priority of card handling is in this order:

```
Brave Chains > Chain Priority > Servant Priority (if on) > Card Priority 
```

This means that the [Brave Chain](#brave-chain) option is of the highest importance. This interaction with Chain Priority will be elaborated on in the corresponding sections.

|         Simplified Brave Chain flowchart / decision tree          | 
|:-----------------------------------------------------------------:|
| ![Brave Chain flowchart](../img/attack/brave-chain-flowchart.png) |

Please refer to the [Brave Chain](#brave-chain) section for more information on what each Brave Chain option entails. 

#### Mighty Chain

|                  Simplified Mighty Chain flowchart                  | 
|:-------------------------------------------------------------------:|
| ![Mighty Chain flowchart](../img/attack/mighty-chain-flowchart.png) |

Mighty Chains, by definition, require all 3 card types in a single chain to be executed. Mighty Chains can be done with multiple Servants or with a single Servant (thus forming a Brave Chain). The order of Mighty Chain cards is currently determined using the [Card Priority](#card-priority-optional). Thus, the default order is BAQ (Buster, Arts, Quick). 

<details><summary>Advanced note</summary>

> Since the default order is determined using Card Priority, it also takes into account Weak and Resist. Thus, if a weak card happens to be an Arts card and there is no weak Buster, since weak Arts is before a neutral Buster, the Mighty Chain will be ABQ.
> This slightly strange interaction is something to note when preparing the configuration, though it should rarely occur.

</details>

| Brave Chains options | Interactions                                                                                                                                                                                                                    |
|:---------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Don't care           | The default output of Mighty Chains will be used, regardless of whether a Brave Chain is used or not. A Mighty Chain will be used if there is one available, otherwise it will skip to the next Chain option.                   | 
| With NP              | Will always attempt to make a Mighty Brave Chain only when a single NP is being used.<br>If there is both a Brave Chain and a Mighty Chain separately available, the Brave Chain will be used over the Mighty Chain.            |
| Always               | Will always attempt to make a Mighty Brave Chain if one is available, regardless of the Servant.<br>If there is both a Brave Chain and a Mighty Chain separately available, the Brave Chain will be used over the Mighty Chain. |
| Avoid                | Will actively avoid making Brave Chains. If the only Mighty Chain is a Mighty Brave Chain, it will be skipped.                                                                                                                  |

#### Buster / Arts / Quick Chain

|         Simplified Buster / Arts / Quick Chain flowchart          | 
|:-----------------------------------------------------------------:|
| ![Color Chain flowchart](../img/attack/color-chain-flowchart.png) |

A $${\color{red}Buster}$$ / $${\color{blue}Arts}$$ / $${\color{green}Quick}$$ Chain is made by choosing 3 cards of the same type. This is referred to a Color Chain for this system. 

| Brave Chains options | Interactions                                                                                                                                                                                                                 |
|:---------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Don't care           | The default output of Color Chains will be used, regardless of whether a Brave Chain is used or not. A Color Chain will be used if there is one available, otherwise it will skip to the next Chain option.                  | 
| With NP              | Will always attempt to make a Color Brave Chain only when a single NP is being used.<br>If there is both a Brave Chain and a Color Chain separately available, the Brave Chain will be used over the Color Chain.            |
| Always               | Will always attempt to make a Color Brave Chain if one is available, regardless of the Servant.<br>If there is both a Brave Chain and a Color Chain separately available, the Brave Chain will be used over the Color Chain. |
| Avoid                | Will actively avoid making Brave Chains. If the only Color Chain is a Color Brave Chain, it will be skipped.                                                                                                                 |

#### Avoid Chain

|                 Simplified Avoid Chain flowchart                  | 
|:-----------------------------------------------------------------:|
| ![Avoid Chain flowchart](../img/attack/avoid-chain-flowchart.png) |

The default behaviour of Avoid Chain in the system is to only avoid Mighty Chain and Color Chain as much as possible. This is the option, that, when combined with 'Avoid Brave Chains', tries its best to completely avoid any kind of chain possible. When it is not possible to avoid a Chain, it will be skipped over.

The default implementation of this system attempts to stagger Servant cards if there are at least 2 Servants available that can make a non-Chain. This is due to the expected use case being to use this option only when face cards are to be used for efficient farming of simple and easy maps like Fuyuki.

| Brave Chains options | Interactions                                                                                                                                                          |
|:---------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Don't care           | The default only avoids Mighty and Color Chains. Thus, Brave Chains will occasionally occur with this option picked.                                                  | 
| With NP              | Attempts to make a Brave Chain (if one is available) only when a single NP is used, while avoiding Mighty and Color Chains.<br>                                       |
| Always               | Will attempt a Brave Chain while still avoiding a Mighty or Color Chain.<br>While the use-case of this option is near zero, it is still something that can be picked. |
| Avoid                | Will actively avoid making Brave Chains. The most common use case.                                                                                                    | 

___  

### Brave Chain

There are only 3 options for brave chains, unless [Chain Priority](#chain-priority) is on.

![Battle Configs - Card Priority | Brave Chain menu](../img/attack/brave-chain-menu.png "Battle Configs - Card Priority | Brave Chain menu")

![Battle Configs - Card Priority | Brave Chain options](../img/attack/brave-chain-options.png "Battle Configs - Card Priority | Brave Chain options")

| Option                                                    | Description                                                                                                                                                                        |
|:----------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Don't care                                                | Will not attempt to form nor avoid any Brave Chains and will simply use whatever order other priorities have sorted in. Thus, there can be the occasional Brave Chains that occur. | 
| With NP                                                   | Will always attempt to make a Brave Chain when a single NP is being used. If no NPs are used, Brave Chain checking is ignored.                                                     |
| Always (only if ([Chain Priority](#chain-priority) is on) | Will always attempt to make a Brave Chain when one is available, regardless of the Servant.                                                                                        |
| Avoid                                                     | Will actively avoid making Brave Chains if one is available, unless it is absolutely impossible (e.g. all cards belong to a single Servant)                                        |

___  

### Rearrange Cards

When you pick 3 face cards in FGO, the position of the card determines the damage and effect of the card.  
For example, Quick cards generate more stars, Arts cards generate more NP, and Buster cards deal more damage.

At the same time, the 1st card will determine the bonus effect applied to all face cards. For example, if a  
Buster card is picked first, all face cards get a damage boost.

The Rearrange Cards feature orders the 3 strongest cards so the 2nd strongest is used last.

![Face Cards](../img/face-cards.jpg)

If default card priority is used, FGA will pick these 3 cards:
- Jeanne Archer Buster (strongest card)
- Castoria Buster (3rd strongest card)
- Jeanne Archer Arts (2nd strongest card)

___  

## Support Selection

### Class Selection

If you do not select any class, FGA will pick support from the current class that is showing in the screen.

![Battle Configs - Support Selection | Class Selection](https://i.imgur.com/fM3HW6p.png "Battle Configs - Support Selection | Class Selection")

Be sure to click on the class that you want to select.

![click class](https://i.imgur.com/yRsgVGU.png)

If you want to check the `All` option toggle this button.

![All](https://i.imgur.com/rKsjhgd.png)

:warn: This feature does not work reliably for users who have not cleared Solomon. The "Start Quest" button  
will become unresponsive until you manually click the Back button in the top left.

### Support Selection Options

There are 3 ways to select support.

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

![Battle Configs - Support Selection | Preferred Selection](https://i.imgur.com/om1dM9K.png "Battle Configs - Support Selection | Preferred Selection")

Then, you can set the preferred settings.

#### Preferred Selection - Preferred Servant

Click on the Preferred Servants option to show a dialog where you can select your wanted Servants.

![Servants](https://i.imgur.com/G2aheeG.png)

Then you can select if you want the preferred servant to be `max ascended` and which skills to be `max level`

![Servant Settings](https://i.imgur.com/NRpwsuV.png)

Note: FGA can only reliably detect if the skill level is max level `Lvl. 10` or not. FGA can't detect the current skill level.

#### Preferred Servant - Preferred CE

Click on Preferred CE and select the wanted CEs.

![CE](https://i.imgur.com/017VHsd.png)

Then you can select if you want the preferred CE to be `max limit broken`.

![CE Settings](https://i.imgur.com/SimCcz4.png)

However, for events it's easier to use the in-game CE filter instead of creating event CE images  
with the Support Image Maker.

#### Preferred Servant - Preferred Friends

Enable this option if you want to use Support Servants from specific friends.

This is useful if level 90 NP1 Servants are not strong enough for the farming quest. Since FGA can't detect  
Servant Levels, NP levels and Append Skill levels, this allows you to filter for those using friend names.

Of course, FGA won't be able to select non-friend Servants who would fulfill those criteria.

If there are no images in the `support` -> `friends` directory, it will look like this:

![Preferred Friends](https://i.imgur.com/GAq5wIK.png)

Once you have created images via the Support Image Maker, you can select your friends.

![Preferred Friends](https://i.imgur.com/yJGbnsn.png)

#### Final Look

![Preferred Selection](https://i.imgur.com/DSjRziN.png)

#### Fallback

If the preferred support is not found, FGA will refresh the support list according to the settings in  
More Options -> Advanced -> Fine-Tune -> Support.

![Battle Configs - Support Selection | Preferred Selection | Fine-Tune](https://i.imgur.com/Ewh6o4r.png "Battle Configs - Support Selection | Preferred Selection | Fine-Tune")

After the limit was reached (e.g. number of refreshes), FGA will act depending on what is set in the fallback option.

![Fallback](https://i.imgur.com/1z9dP1t.png)
