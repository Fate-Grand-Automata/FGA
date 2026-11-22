Spam makes FGA use skills and NPs whenever they are ready, on top of the [command](Command-Editor). It's useful for long fights where you don't want to plan every turn, like raids or challenge quests.

Open it with **Spam** in a [battle config](Battle).

Spam actions happen after the command's actions of a turn. A skill is only used if its icon shows that it's ready.

## Settings

Spam is configured per team position. Pick the Servant position (1 to 6) at the top, then set:

- **NP** and each skill (**S1** to **S3**):
  - **Spam mode:**
    - **None:** never use it.
    - **Spam:** use it whenever it's ready.
    - **Danger:** only use it when an enemy shows a danger or Servant icon. Needs **Auto choose target**.
  - **Waves:** the waves to spam in.
- Skills also have a **Target**: None, Self, Slot1 to Slot3, Left or Right (for [NP Type (2)](Command-Editor#np-type-2) skills).

**PRESETS** set all Servants at once: SPAM ALL, SPAM ALL DANGER and NO SPAM.

## Auto choose target

When on, FGA targets the dangerous enemy each turn: the right-most enemy showing a danger or Servant icon.
Danger mode only works with it.

See [#510](https://github.com/Fate-Grand-Automata/FGA/issues/510) for more background on how spam works.
