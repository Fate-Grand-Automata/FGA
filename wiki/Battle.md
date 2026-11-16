The Battle script repeats a quest: it picks a support, plays the battle according to your battle config, collects the rewards, refills AP and starts the next run.

- [Starting a battle](#starting-a-battle)
  - [Refills](#refills)
  - [Limits](#limits)
- [Battle Configs](#battle-configs)
  - [Creating a config](#creating-a-config)
  - [Config settings](#config-settings)
  - [Materials](#materials)
  - [Server](#server)
  - [Party](#party)
  - [Raid](#raid)
  - [Shuffle cards](#shuffle-cards)
  - [Sharing configs](#sharing-configs)

# Starting a battle

Press PLAY on one of these screens:

- The quest list, with the quest you want to farm at the **top**. FGA always taps the uppermost quest.
- The support selection screen of the quest.
- The "Repeat quest" dialog after a battle.
- During a battle.

The dialog lists your battle configs on the left. Pick one and press OK.
Only configs whose [Server](#server) matches the current game server are shown.

## Refills

- **Refill:** how many times to refill AP. The count goes down by the refills used, so it carries over to your next session.
- **Apple type:** Copper, Blue, Silver, Gold or SQ. Tap the selected type again to turn refills off. SQ is hidden unless you turn off `Hide SQ in AP Resources` in [More Options](More-Options#battle).
- **Wait for AP Regeneration:** when the refills are used up, wait for AP to regenerate instead of stopping.

## Limits

Each limit is a checkbox with a number. The script stops when it reaches one:

- **Runs:** number of completed runs.
- **Materials:** number of dropped materials. Only the [Materials](#materials) selected in the battle config count.
- **CEs:** number of dropped CEs.

Limits count down and carry over to the next session. Once one reaches 0, it turns itself off. **Reset all** turns all limits off.

The dialog saves everything when it closes, even when you press Cancel.

# Battle Configs

## Creating a config

On the FGA main screen, open **Battle Configs**.

![Battle Configs image](https://i.imgur.com/gHvukdq.jpeg "Battle Configs")

Create a new config with the floating action button.

![Battle Configs - Create New Script](https://i.imgur.com/9zUDLWf.jpeg "Battle Configs - Create New Script")

Give it a name. The notes are optional, for example which Servants the config is meant for.

![Battle Configs - Name Script](https://i.imgur.com/O108gg6.jpeg "Battle Configs - Name Script")

Then record the command, which tells FGA which skills and NPs to use in which wave. Tap **Command** to open the [Skill Maker](Skill-Maker).

![Battle Configs - Command](https://i.imgur.com/2pzGUNp.jpeg "Command")

The terminal icon next to it lets you type or paste a command as text instead.

## Config settings

| Setting | Page |
|---|---|
| Command | [Skill Maker](Skill-Maker) |
| Materials, Server, Party, Raid | below |
| Spam | [Spam](Spam) |
| Card Priority | [Card Priority](Card-Priority) |
| Support | [Support Selection](Support-Selection) |
| Shuffle cards | [below](#shuffle-cards) |

Everything except the command is optional.

## Materials

Select the materials you want to farm. FGA counts their drops for the **Materials** [limit](#limits) and shows them when the script ends.
With nothing selected, the cell shows "Don't count".

![Battle Configs - Material](https://i.imgur.com/ctteami.jpeg "Battle Configs - Material")

![Material Selection](https://i.imgur.com/2IQKvSj.png)

Selected materials move to the top of the list.

![Selected Material](https://i.imgur.com/FGyjJng.png)

## Server

If you play on several servers, set the server a config is meant for. The PLAY dialog only lists configs for the current server, plus the configs without a server.
The config list also gets one tab per server.

![Battle Configs - Server Selection](https://i.imgur.com/ihEg1WS.jpeg "Battle Configs - Server Selection")

## Party

Pick one of the 15 party slots. Before each quest, FGA switches to that party if it isn't already selected. **Default** keeps whatever party is selected.

![Battle Configs - Party Selection](https://i.imgur.com/ACFI8kc.jpeg "Battle Configs - Party Selection")

## Raid

**Raid Turn Delay** waits an extra 1 to 10 seconds at the start of every turn, for raid battles.
Also see [Raid battles](Skill-Maker#raid-battles) in the Skill Maker.

## Shuffle cards

Shuffles the command cards with **Master Skill 3** when the cards are bad. The 3rd skill of your Mystic Code must be a card shuffle.

- **When:**
  - **No effective cards:** none of the cards is effective against the enemy.
  - **No cards matching NP:** no card belongs to a Servant whose NP is used this turn.
- **Wave:** the wave to check in. FGA shuffles at most once per battle.

This doesn't work if `Skip servant face-card checks` is on in [More Options](More-Options#battle).

## Sharing configs

The menu in the top right of a config has **Export**, **Copy** and **Delete**. Export saves the config as a `.fga` file.
The config list has **Import** and **Export all**.
