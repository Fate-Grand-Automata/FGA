# FateGrandAutomata
Fate/Grand Order is all about farming.
We already have an awesome auto battle script named [Fate-Grand-Order_Lua](https://github.com/29988122/Fate-Grand-Order_Lua) which uses AnkuLua (an implementation of Sikuli using Lua language for scripting on Android).
The only problem I had was that, in trial versions, AnkuLua limits scripts to run for a maximum of 30 min and after which there is a cooldown period.
Any of the serious farmers out there know that this is not enough for the lottery events.
So, being the F2P that I am, instead of buying an AnkuLua subscription, I thought why not make something similar.
So, I present **FateGrandAutomata** (I suck at naming things) which is pretty much a C# port of **Fate-Grand-Order_Lua** but has an inbuilt Sikuli-like API.
It runs on Xamarin.Android, uses OpenCV for image recognition, uses Android's Acessibility and Media Projection APIs.