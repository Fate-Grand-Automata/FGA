## Nox

On Nox, FGA needs Root to work since MediaProjection doesn't seem to work.

- Make sure you're using an **Android 7** instance of Nox.
  Use [MultiDrive](https://www.bignox.com/blog/how-to-run-multiple-android-instances-with-nox-app-player/) for managing/creating Nox instances.
- Go to `More options` in the app and turn ON `Use Root for Screenshots`.
- You also need to enable `Root` from Nox's settings. See [How to root Nox](https://www.bignox.com/blog/how-to-root-nox-app-player/).

## Bluestacks

These settings are tested, some other may also work:

- Install Bluestacks X
- Start the Multi-Instance Manager as Admin and create a 64-bit Pie instance in the 
- DON'T use root for screenshots
- Change to High Performance mode:
  - Open Bluestack's settings
  - Go to `Performance` tab.
  - Set `Performancer mode` to `High Performance` (For some installations, this breaks Bluestacks as soon as you start the FGA service and you need to enable `Compatibility` mode instead)
  - Save settings and Restart Bluestacks.
- Install FGA from the Play Store, not from Github
- In order to start the FGA service, rotate the screen to portrait mode first. See [this comment](https://github.com/Fate-Grand-Automata/FGA/issues/967#issuecomment-974652785) for more details.

## MEmu

- Enable Root Mode in the MEmu's `Engine` settings
- Enable `Use Root for Screenshots` under `More options` -> `Advanced`

## LDPLAYER 

- Works without needing any changes to ldplayer settings 