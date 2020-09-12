# Fate/Grand Automata
[![CI](https://github.com/MathewSachin/Fate-Grand-Automata/workflows/CI/badge.svg?branch=master&event=push)](https://github.com/MathewSachin/Fate-Grand-Automata/actions)

![Logo](app/src/release/res/mipmap-xxxhdpi/ic_launcher_round.png)

Auto-battle app for F/GO (Android 7 or later, no need for Root).  

<a href='https://play.google.com/store/apps/details?id=com.mathewsachin.fategrandautomata&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' width="175" src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

This is a **Kotlin** port of [FGO-Lua][FGOLua] as an Android app with UI for configuration and without a time-limit on use.  
It doesn't tamper with the game in anyway and works by looking at the screen and tapping things just like a normal user would do.  
It's not made to do the story for you, but to automate the mundane farming.

Having Trouble? See the [Troubleshooting Guide](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Troubleshooting) first.  
Join us on our [GamePress thread](https://community.gamepress.gg/t/automatic-farming-app-fate-grand-automata/72155) for discussions.

## Video Guide by @reconman

[![Watch the video guide](https://img.youtube.com/vi/je-FSHBFGys/sddefault.jpg)](https://www.youtube.com/watch?v=je-FSHBFGys)

## Why make another?
FGO-Lua is really great, but:
1. Uses AnkuLua, so isn't free. Free version has 30min time-limit.
2. Learning curve for configuring the scripts is steeper.
3. Difficult to edit the script on your phone. You can do it using Text editor apps, but it's not fun, trust me.

Making the app was a great learning experience and something different from your usual Android projects.

## How does it work?
This is a native Android app written in Kotlin.
We use [OpenCV](https://opencv.org/) for image recognition,
[Media Projection](https://developer.android.com/reference/android/media/projection/MediaProjection) for taking screenshots
and [Accessibility Service](https://developer.android.com/guide/topics/ui/accessibility) for clicking/swiping.

## How to Use?
1. Install from the Play Store link given above and launch the app.
2. Click on `Toggle Service` and give all the permissions it asks for.
3. Now, you can see a button with play icon on it floating on screen.
4. Open F/GO and Go to the node you want to farm.
5. Press Play to start. The same button can be pressed to stop later.

## How to configure?
For how the settings work, see the original [FGO-Lua][FGOLua] project.

Though there are many more features added in this project, we didn't get time to make proper documentation yet.

## How to make/use images of Servant/CE/Friend?

See the wiki page for [Support Image Maker](https://github.com/MathewSachin/Fate-Grand-Automata/wiki/Support-Image-Maker).

## What about other scripts like Lottery and Friend Gacha?
There is an option in Settings called `Script Mode`.
It defaults to `Battle`.
Set it to the script you require.

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