# Fate/Grand Automata
Fate/Grand auto-battle app for Android.
This is pretty much a C# port of [Fate-Grand-Order_Lua][FGOLua] but has an inbuilt Sikuli-like API.
Also, includes a UI for configuring Settings.

## Why make another?
Fate/Grand Order is all about farming.
We already have an awesome auto battle script named [Fate-Grand-Order_Lua][FGOLua] which uses [AnkuLua](https://ankulua.boards.net/) (an implementation of [Sikuli](http://doc.sikuli.org/sikuli-script-index.html) using Lua language for scripting on Android).
The only problem I had was that, in trial versions, AnkuLua limits scripts to run for a maximum of 30 min and after which there is a cooldown period.
Any of the hardcore farmers out there know that this is not enough for the lottery events.
So, being the salty F2P that I am, instead of buying an AnkuLua subscription, I thought why not make something similar.
This was a great learning experience and something different from your usual Android projects.

## How does it work?
It runs on [Xamarin.Android](https://docs.microsoft.com/en-us/xamarin/android/), uses [OpenCV](https://opencv.org/) for image recognition, uses Android's [Acessibility](https://developer.android.com/guide/topics/ui/accessibility) and [Media Projection](https://developer.android.com/reference/android/media/projection/MediaProjection) APIs.
Battery optimizations can screw up the functionality of the app. App tries to disable them automatically but you might also have to do that manually.

The icons are from https://materialdesignicons.com/

## What to do about bugs?
This is a lazy project (see how the commits are named :laughing:) and I'm a beginner in Android.
I made it solely for the purpose of running on my phone.
You can use the code if you want, but I can't really help you to debug it on your phone if you face any problems.
Debugging really is a pain due to FGO not allowing `Developer Mode` to be `ON` during execution.
Though, I welcome contributions if you could figure out the cause of the problem.

**Note:** If your device has a notch, Don't rotate your screen when the script is running.

## How to Use?
1. Download from [GitHub Releases](https://github.com/MathewSachin/FateGrandAutomata/releases/). I can't put this app on `Play Store` due to some restrictive features this app uses.
   The APK available there might not always be the latest one. This README is written according to the latest commit of the code.
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

Autoskill configuration can be created at `Settings/AutoSkill/Manage Autoskill configurations`.  
You won't be prompted at script start for the config to use. You need to set the `Settings/AutoSkill/Selected Autoskill Config` value.
If you've set a Servant or CE in the currently selected Autoskill configuration and Autoskill is Enabled, then support selection mode is assumed to be preffered.

## Where to put Servant/CE images?
The app only comes with `mona_lisa.png` and `chaldea_lunchtime.png`.
If you want to use any other preferred servant or ce, you need to manually include their images.
Put Servant images in `Fate-Grand-Automata/support/servant` folder and
CE images in `Fate-Grand-Automata/support/ce` folder.
Instead of having to select multiple ascension images for a servant, you can put all the images of a servant in a folder and select the folder in settings.
Create the folder if it doesn't already exist.
For information on how to use support selection, see the original [Fate-Grand-Order_Lua][FGOLua] project.

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

## Upcoming Features
1. Auto-giftbox.

## Like the project? Want to support me?
Just pray that I can roll [Skadi](https://gamepress.gg/grandorder/servant/scathachskadi)!

[FGOLua]: https://github.com/29988122/Fate-Grand-Order_Lua