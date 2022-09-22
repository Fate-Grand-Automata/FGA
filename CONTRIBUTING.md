
#  Contribution Guide

Do you want to contribute to the source code? If you are interested, take a look at the [open issues](https://github.com/Fate-Grand-Automata/FGA/issues).

##  Getting Started

A basic knowledge of Kotlin and Android development is recommended. For development tools, [Android Studio](https://developer.android.com/studio) and a working emulator or Android phone are necessary to test changes.

If you are new to Kotlin and Android development, you can find out more here:

- [Kotlin](https://kotlinlang.org/docs/getting-started.html)
- [Android](https://developer.android.com/courses/android-basics-kotlin/course)

Once you're ready:

1. Fork the repository and clone it to your local machine.
2. Open the project with Android Studio.
3. If you are working on a specific issue, it is good practice to create and stage changes on a new branch based on the issue name.
4. You can create a new branch by using the following commands: `git checkout -b issue-name`.
5. If you want to debug your app, you should set the [versionCode](https://github.com/Fate-Grand-Automata/FGA/blob/ba8d04edc7c6a13b9c519cd102566574c00f2506/app/build.gradle#L38) to the value of your currently installed FGA version. The default value is 1 and Android does not allow downgrading apps to older versions.

  

Project Structure:

- `./app/`: Android-based part of the app that holds the UI, MediaProjection, AccessibilityService, and other utilities.
- `./libautomata/`: Contains the methods needed to write the scripts. Method implementation is dependency-injected.
- `./prefs/`: Implements user preferences
- `./scripts/`: Contains the scripts for app functions, like AutoBattle, AutoLottery, etc.

## How FGA searches for images
When FGA takes screenshot, the following steps are executed:
1. A screenshot is taken in the device's original resolution
2. The screenshot is converted to grayscale and scaled to 720p
3. The screenshot is cropped to the game area. This removes blue bars for tablets or black bars in Custom Game Area mode.
4. The screenshot is further cropped to the search region
5. The image to search is converted to grayscale if it's in color.
6. OpenCV is used to search for the given image in the cropped screenshot. The default minimum similarity is 80 %, but this can be changed in the Fine-Tune settings.<br/><br/>Manual similarity values in Region.exists(image) should be avoided in most cases. Anything below 65 % similarity indicates that your image doesn't match the screen.

Because of step 2, any images added to FGA have to be in 720p.
## How FGA deals with Locations/Region coordinates
For greater accuracy, FGA uses 1440p coordinates instead of 720p used by image detection.

The Location x=0, y=0 is the top left corner of the 3rd step in [How FGA searches for images](#how-fga-searches-for-images).

y=1440 is on the bottom of the screen, but the x coordinate of the right border depends on the screen ratio of the device. For example, on a 16:9 screen (1280x720 or 1920x1080), the bottom right is located at x=2560, y=1440. On a 18:9 screen, (2560x1080), it's located at x=3413, y=1440. (2560 / 1080 * 1440)

The Location and Region methods .xFromCenter() and .xFromRight() will help you by treating x=0 as the center or right edge of the screen, respectively. Similar methods exist for y, but are rarely used in features.

Guide for determining new coordinates:
1. Take a screenshot of the screen you want to handle.
2. Open the image in the free online [Photopea editor](https://www.photopea.com/) or Adobe Photoshop if you have it. The next steps assume you're using Photopea.
3. Select View -> Rulers to enable rulers on the sides. These will help you with cropping and measuring.
4. If you have any black or blue bars, remove them. This works best by zooming to 350 % or more. Then create a blue line by clicking and holding the left mouse button from the left ruler. If you hold the Shift button at the same time, the blue lines will always land on full pixel positions. Put blue lines on the edges of the blue/black bars.<br/><br/>If you have bars on the left **and** right, you should now have 2 blue lines, 1 on the left and 1 on the right. Your mouse will snap to their position if you're near them.<br/><br/>Reduce your zoom so you can see the whole screenshot. Then switch to the selection tool with the dotted rectangle button on the left or pressing M. Drag a rectangle from the top of the left blue line to the bottom of the right blue line (or the edge of your image if you only have a black bar on the left). The blue lines should help you with hitting the exact positions. Then open the Image Menu on the top and select Crop, the blue/black bars should now be gone.
5. Choose Image -> Image Size and enter 720 in the Height field, then click OK. Your image is now scaled to 720p. Even though FGA coordinates are measured in 1440p, 720p will help you with extracting new images.
6. Make sure the Move tool is selected by either clicking on the Mouse Cursor button in the top left or by pressing V. Click and drag from the left ruler to create vertical blue lines, click and drag from the top ruler to create horizontal lines. The tooltip will tell you the X and Y positions of your blue lines. Write the positions down somewhere. If you need to check the position again, click and drag on them.
7. Now that you have the X and Y positions, double them to convert them from 720p (because of step 5) to 1440p. For example, x=650 turns into x=1300.
8. For most Locations/Regions, the coordinates will have to be further adjusted because many FGO buttons and windows stick to the center. For example, the button for confirming Support list updates is always 100 pixels (or 200 pixels in 1440p) to the right of the center of the screen.<br/><br/>For those situations, you will have to subtract 640 from the X position (image width / 2). If your X value is on the left side of the screen, it will become negative with the correction. Then use Location.xFromCenter() and Region.xFromCenter() to tell FGA about your centered coordinates.
##  How do I use my emulator (Bluestacks/Nox/Memu) in Android Studio?

ADB tools are required to use a custom emulator in Android Studio.

Enable USB debugging in your emulator, and then open up a terminal/command prompt in the location that

contains the ADB tool. Type in the command `adb connect localhost:####`, where the numbers are default ports specified by the emulator.

For example, for Bluestacks you would use `adb connect localhost:5555`.
