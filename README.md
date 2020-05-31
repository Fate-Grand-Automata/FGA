# Fate/Grand Automata
[![CI](https://github.com/MathewSachin/Fate-Grand-Automata/workflows/CI/badge.svg?branch=master&event=push)](https://github.com/MathewSachin/Fate-Grand-Automata/actions)

<div style="text-align:center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png">
</div>

Auto-battle app for Fate/Grand Order (Android 7 or later, no need for Root).  

Download from our [Website](https://MathewSachin.github.io/Fate-Grand-Automata).

This is pretty much a **Kotlin** port of [Fate-Grand-Order_Lua][FGOLua] with UI for configuring Settings and inbuilt Sikuli like API.
And there's no time limit on the use of the app unlike FGO-Lua.

[Running on Samsung devices](https://MathewSachin.github.io/Fate-Grand-Automata/SAMSUNG.html) |
[Running on Emulators](https://MathewSachin.github.io/Fate-Grand-Automata/EMULATORS.html)

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

## What to do about bugs?
Debugging really is a pain due to FGO NA not allowing `Developer Mode` to be `ON` during execution.
Use GitHub issues to report bugs and try to be specific about the problem.
Also, list information like your phone's model number, screen size, Android version, whether you have a notch.

## How to Use?
1. Download from the [Website](https://MathewSachin.github.io/Fate-Grand-Automata). I can't put this app on `Play Store` due to some restrictive features this app uses.
2. Install on your phone. You'll need to enable installing apps out of `Play Store`.
3. Launch the app. Grant it permissions it asks for: `Read External Storage`, `Write External Storage`, `Ignore Battery Optimizations`.
4. Click on `Toggle Service`. You would be prompted to turn on the `Accessibility Service` and taken to `Accessibility Settings` page.
   If it is already ON, turn it OFF and then turn it ON again.
   If you turned ON `Accessibility Service` right now, click again on `Toggle Service`.
5. You would be asked for `Media Projection` permission.
6. Now, you can see a button with play icon on it floating on screen.
7. Open F/GO and Go to the node you want to farm.
8. Press Play to start. The same button can be pressed to stop later.

## How to configure?
For how the settings work, see the original [Fate-Grand-Order_Lua][FGOLua] project.

AutoSkill configuration can be created at `Settings/AutoSkill/Manage AutoSkill configurations`.  
If you've set a Servant or CE in the selected AutoSkill configuration and AutoSkill is Enabled, then support selection mode is assumed to be preferred.

## Where to put Servant/CE images?
Put Servant images in `Fate-Grand-Automata/support/servant` folder and
CE images in `Fate-Grand-Automata/support/ce` folder.

The app has some common Servant/CEs inbuilt. You can extract them using the `Extract Default Support Images` option.
The app should extract them automatically when you go to the Support settings screen.
If you want to use any other preferred servant or ce, you have to create their images using `Support Image Maker` script.

Instead of having to select multiple ascension images for a servant, you can put all the images of a servant in a folder and select the folder in settings.

Here's what an example directory structure looks like:

```
Fate-Grand-Automata/
-- support/
-- -- servants/
-- -- -- ozy4.png
-- -- -- melt4.png
-- -- -- Merlin/
-- -- -- -- merlin1.png
-- -- -- -- merlin2.png
-- -- -- -- merlin3.png
-- -- -- -- merlin4.png
-- -- -- -- merlin_c.png
-- -- -- Waver/
-- -- -- -- waver1.png
-- -- -- -- waver2.png
-- -- -- -- waver3.png
-- -- -- -- waver4.png
-- -- ce/
-- -- -- kscope.png
-- -- -- black_grail.png
```

## What about other scripts like Lottery and Friend Gacha?
There is an option in Settings called `Script Mode`.
It defaults to `Battle`.
Set it to the script you require.

## How to use Support Image Maker?
`Support Image Maker` automatically creates images from the Support screen that can be used with the script.
You can also use it from the Friend List which is easier since you don't have to keep refreshing till the desired Servant/CE shows up.

1. Set the `Script Mode` option in Settings to `Support Image Maker`.
2. Now, click on `Toggle Service` button, the `Play` button shows itself.
3. Open F/GO. Go to support selection screen and ensure that the Servant/CE you want is visible (It is important that the complete Servant + CE region is visible).
4. Click on Play. The script should exit almost immediately. Images are saved to `Fate-Grand-Automata/support` folder.
5. Rename the files to whatever you want and then use with Auto Support Selection.

## Like the project? Want to support me?
Just pray that I can roll [Skadi](https://gamepress.gg/grandorder/servant/scathachskadi)!
Oh, and code/doc contributions are surely welcome!

## Acknowledgements
- [FGO-Lua][FGOLua] developers are the real deal. Without them this app won't exist.
- We're using OpenCV Android package from: https://github.com/iamareebjamal/opencv-android
- The icons are from https://materialdesignicons.com/
- Drag-sort logic on Card Priority screen is thanks to https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd

[FGOLua]: https://github.com/29988122/Fate-Grand-Order_Lua