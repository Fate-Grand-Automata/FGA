## Install

Download FGA from the [website](https://fate-grand-automata.github.io).
Prefer the Google Play version: it updates automatically and isn't affected by Android's restrictions on Accessibility Services for sideloaded apps.

There is a [video guide](https://youtu.be/JOwupZ4W8AQ). It shows an older version of the app, but the overall flow is the same.

## First launch

The first time you open FGA, it walks you through:

1. **App language.**
2. **Choose Folder:** a folder FGA stores its files in, such as support images, drop screenshots and recordings. Create a new, empty folder such as `FGA`. FGA extracts the default [support images](Support-Image-Maker) into it.
3. **Battery optimization:** disable it for FGA. Otherwise Android may kill the service in the background, and the PLAY button disappears. See [dontkillmyapp.com](https://dontkillmyapp.com) for your phone brand.

## Start the service

1. In FGA, tap **Start Service** in the bottom-right corner.
2. Grant what it asks for:
   - **Accessibility Service**, so FGA can tap and swipe.
   - **Display over other apps**, for the PLAY button.
   - **Screen capture**, so FGA can see the game.
3. Open FGO. The floating PLAY button appears as soon as the screen is in landscape.

Place the PLAY button in the bottom-left corner. Anywhere else, it may cover something FGA needs to see.

## Run a script

1. Go to the screen in FGO where you want to start, for example the quest list for [Battle](Battle).
2. Press PLAY. FGA detects the current screen and shows the matching dialog:

| Screen in FGO | Script |
|---|---|
| Friend Point summon screen, or its results | [Friend Point Summon](Friend-Point-Summon) |
| Event lottery | [Lottery](Lottery) |
| Present Box, filtered to EXP cards | [Gift Box](Gift-Box) |
| Servant Enhancement or Ascension screen | [Servant Enhancement](Servant-Enhancement) |
| Skill Enhancement screen | [Skill Enhancement](Skill-Enhancement) |
| Support selection or Friend list | [Battle](Battle), with an extra [Support Image Maker](Support-Image-Maker) button |
| Anything else | [Battle](Battle) |

3. Choose your options and press OK.

While a script runs, press the button again to pause it. When paused, you can resume, stop, or check the current status.
When a script ends, FGA shows why it stopped, for example "AP ran out" or "Inventory Full".

Before your first battle, create a battle config: see [Battle](Battle).

## Main screen

- **Battle Configs:** create and edit your [battle configs](Battle).
- **App Language**
- **More options:** see [More Options](More-Options).
- **Accessibility Service:** shows whether the service is enabled.
- The header has links to Troubleshooting, Discord, and donations. Tap the build number to see the releases.
