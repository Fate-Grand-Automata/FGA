# Fate/Grand Automata
[![CI](https://github.com/MathewSachin/Fate-Grand-Automata/workflows/CI/badge.svg?branch=master&event=push)](https://github.com/MathewSachin/Fate-Grand-Automata/actions)

![Logo](app/src/release/res/mipmap-xxxhdpi/ic_launcher_round.png)

Auto-battle app for F/GO (Android 7 or later, no need for root on phones).  

Download from our [website](https://fate-grand-automata.github.io)

This is a **Kotlin** port of [FGO-Lua][FGOLua] as an Android app with UI for configuration and without a time-limit on use.  
It doesn't tamper with the game in anyway and works by looking at the screen and tapping things just like a normal user would do.  
It's not made to do the story for you, but to automate the mundane farming.

Having Trouble? See the [Troubleshooting Guide](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Troubleshooting) first.  
Join us on our [GamePress thread Part 2](https://community.gamepress.gg/t/fate-grand-autamata-discussion-and-help-thread-round-2/107040) for discussions.  
Old Gamepress thread: [GamePress thread](https://community.gamepress.gg/t/automatic-farming-app-fate-grand-automata/72155)

## Video Guide by @reconman

[![Watch the video guide](https://img.youtube.com/vi/JOwupZ4W8AQ/sddefault.jpg)](https://youtu.be/JOwupZ4W8AQ)


## How to Use?
1. Install from the link given above and launch the app.
2. Click on `Start Service` and give all the permissions it asks for.
3. Open FGO. Now, you can see a button with play icon on it floating on screen.
4. Go to the node you want to farm.
5. Press Play to start. The same button can be pressed to pause/stop later.

Check the [Troubleshooting Guide](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Troubleshooting) first if you face any problems.

## How to make/use images of Servant/CE/Friend?

See the wiki page for [Support Image Maker](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Support-Image-Maker).

## What about other scripts like Lottery and Friend Gacha?
When you click on the PLAY button, the app detects which script can be run on the current screen and presents it to you.

## How does it work?
This is a native Android app written in Kotlin.
We use [OpenCV](https://opencv.org/) for image recognition,
[Media Projection](https://developer.android.com/reference/android/media/projection/MediaProjection) for taking screenshots
and [Accessibility Service](https://developer.android.com/guide/topics/ui/accessibility) for clicking/swiping.

## Contributions
Do you want to contribute to the source code? If you are interested, take a look at the [open issues](https://github.com/Fate-Grand-Automata/FGA/issues).
### Getting Started
A basic knowledge of Kotlin and Android development is recommended. For development tools, [Android Studio](https://developer.android.com/studio) and
a working emulator or Android phone are necessary to test changes.

If you are new to Kotlin and Android development, you can find out more here:
- [Kotlin](https://kotlinlang.org/docs/getting-started.html)
- [Android](https://developer.android.com/courses/android-basics-kotlin/course)

Once you're ready:
1. Fork the repository and clone it to your local machine.
2. Open the project with Android Studio.
3. If you are working on a specific issue, it is good practice to create and stage changes on a new branch based on the issue name.
4. You can create a new branch by using the following commands: `git checkout -b issue-name`. 

Project Structure:
- `./app/`: Android-based part of the app that holds the UI, MediaProjection, AccessibilityService, and other utilities.
- `./libautomata/`: Contains the methods needed to write the scripts. Method implementation is dependency-injected.
- `./prefs/`: Implements user preferences
- `./scripts/`: Contains the scripts for app functions, like AutoBattle, AutoLottery, etc.

## Acknowledgements
- [FGO-Lua][FGOLua] developers are the real deal. Without them this app won't exist.
- We're using OpenCV Android package from: https://github.com/iamareebjamal/opencv-android
- The icons are from https://materialdesignicons.com/
- Drag-sort logic on Card Priority screen is thanks to https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd

[FGOLua]: https://github.com/29988122/Fate-Grand-Order_Lua

## Like the project? Want to support me?
~~Just pray that I can roll [Skadi](https://gamepress.gg/grandorder/servant/scathachskadi)!~~ `SUCCESS!`  
Oh, and code/doc contributions are surely welcome!

For donations, see the [wiki](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Donations).
