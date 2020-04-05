# FateGrandAutomata
Fate/Grand auto-battle app for Android.
This is pretty much a C# port of [Fate-Grand-Order_Lua][FGOLua] but has an inbuilt Sikuli-like API.
Also, includes a UI for configuring Settings.

## Why make another?
Fate/Grand Order is all about farming.
We already have an awesome auto battle script named [Fate-Grand-Order_Lua][FGOLua] which uses AnkuLua (an implementation of Sikuli using Lua language for scripting on Android).
The only problem I had was that, in trial versions, AnkuLua limits scripts to run for a maximum of 30 min and after which there is a cooldown period.
Any of the hardcore farmers out there know that this is not enough for the lottery events.
So, being the salty F2P that I am, instead of buying an AnkuLua subscription, I thought why not make something similar.
This was a great learning experience and something different from your usual Android projects.

## How does it work?
It runs on Xamarin.Android, uses OpenCV for image recognition, uses Android's Acessibility and Media Projection APIs.
Battery optimizations can screw up the functionality of the app. App tries to disabled them automatically but you might also have to do that manually.

## What to do about bugs?
This is a project I made solely for the purpose of running on my phone.
You can use the code if you want, but I can't really help you to debug it on your phone if you face any problems.
Debugging really is a pain due to FGO not allowing `Developer Mode` to be `ON` during execution.
Though, I welcome contributions if you could figure out the cause of the problem.

## How to Use?
1. Download from GitHub Releases. I can't put this app on Play Store due to some restrictive features this app uses.
2. Install on your phone. You'll need to enable installing apps out of Play Store.
3. Launch the app. Grant it permissions it asks for: Write External Storage, Ignore Battery Optimizations.
4. Click on `Toggle Service`. You would be prompted to turn on the Accessibility service and taken to Accessibility Settings page.
   If you turned ON Accessibility service right, now click again on `Toggle Service`.
5. Now, you can see a button with play icon on it floating on screen.
6. Open F/GO and Go to the node you want to farm.
8. Press Play to start. The same button can be pressed to stop later.

Settings can be opened from the app's menu (three dots on top-right corner).
For how the settings work, see the original [Fate-Grand-Order_Lua][FGOLua] project.

## Like the project? Want to support me?
Just pray that I can roll Skadi!

[FGOLua]: https://github.com/29988122/Fate-Grand-Order_Lua