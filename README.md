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
Battery optimizations can screw up the functionality of the app. App tries to disabled them automatically but you might also have to do that manually.

## What to do about bugs?
This is a lazy project (see how the commits are named :laughing:) and I'm a beginner in Android.
I made it solely for the purpose of running on my phone.
You can use the code if you want, but I can't really help you to debug it on your phone if you face any problems.
Debugging really is a pain due to FGO not allowing `Developer Mode` to be `ON` during execution.
Though, I welcome contributions if you could figure out the cause of the problem.

## How to Use?
1. Download from [GitHub Releases](https://github.com/MathewSachin/FateGrandAutomata/releases/). I can't put this app on `Play Store` due to some restrictive features this app uses.
   The APK available there might not always be the latest one. This README is written according to the latest commit of the code.
2. Install on your phone. You'll need to enable installing apps out of `Play Store`.
3. Launch the app. Grant it permissions it asks for: `Read External Storage`, `Write External Storage`, `Ignore Battery Optimizations`.
4. Click on `Toggle Service`. You would be prompted to turn on the `Accessibility Service` and taken to `Accessibility Settings` page.
   If it is already ON, turn it OFF and then turn it ON again.
   If you turned ON `Accessibility Service` right now, click again on `Toggle Service`.
5. You would be asked `for Media Projection` permission.
6. Now, you can see a button with play icon on it floating on screen.
7. Open F/GO and Go to the node you want to farm.
8. Press Play to start. The same button can be pressed to stop later.

## How to configure?
Settings can be opened from the app's menu (three dots on top-right corner).
For how the settings work, see the original [Fate-Grand-Order_Lua][FGOLua] project.

Autoskill configuration can be created at `Settings/AutoSkill/Manage Autoskill configurations`.  
You won't be prompted at script start for the config to use. You need to set the `Settings/AutoSkill/Selected Autoskill Config` value.

## Where to put Servant/CE images?
The app only comes with `mona_lisa.png` and `chaldea_lunchtime.png`.
If you want to use any other preferred servant or ce, put their image in `Fate-Grand-Automata/support` folder.
Create the folder if it doesn't already exist.
For information on how to use support selection, see the original [Fate-Grand-Order_Lua][FGOLua] project.

## What about other scripts like Lottery and Friend Gacha?
There is an option in Settings called `Script Mode`.
It defaults to `Battle`.
Set it to the script you require.

## Upcoming Features
1. Auto-giftbox.
2. Support image maker? Automatically crop out servant/ce image from a screenshot.

## Like the project? Want to support me?
Just pray that I can roll [Skadi](https://gamepress.gg/grandorder/servant/scathachskadi)!

[FGOLua]: https://github.com/29988122/Fate-Grand-Order_Lua