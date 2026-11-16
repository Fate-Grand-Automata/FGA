Card Priority tells FGA which face cards to pick after the NPs from the [command](Skill-Maker). It's optional; the default order is `WB, WA, WQ, B, A, Q, RB, RA, RQ`.

Open it with **Card Priority** in a [battle config](Battle).

![Battle Configs - Card Priority](https://i.imgur.com/XbdSqhZ.jpeg "Battle Configs - Card Priority")

## Card order

Drag the cards to reorder them. Cards at the top (HIGH) are picked before cards at the bottom (LOW).

![Battle Configs - Card Priority Interface](https://i.imgur.com/VGszBHI.jpeg)

| Symbol | Card |
|---|---|
| WB, WA, WQ | Buster, Arts, Quick that are effective against the enemy (weak) |
| B, A, Q | Normal Buster, Arts, Quick |
| RB, RA, RQ | Buster, Arts, Quick that the enemy resists |

Examples:

- Damage, Buster first:

  ![Battle Configs - Card Priority | Buster](https://i.imgur.com/0I1BRzp.jpeg "Battle Configs - Card Priority | Buster")

- Charging NP, Arts first:

  ![Battle Configs - Card Priority | Art](https://i.imgur.com/c0IRnTr.jpeg "Battle Configs - Card Priority | Art")

- Critical stars, Quick first:

  ![Battle Configs - Card Priority | Quick](https://i.imgur.com/2a483et.jpeg "Battle Configs - Card Priority | Quick")

## Waves

Wave 1 applies to the whole battle. Add waves (up to 3) with `+` only if you want a different priority in later waves.

![Battle Configs - Card Priority | Default](https://i.imgur.com/N6WfLVB.jpeg "Battle Configs - Card Priority | Default")

Each wave has its own card order, [Brave Chains](#brave-chains) and [Rearrange Cards](#rearrange-cards) settings.

## Servant Priority

Turn on **Use Servant Priority** at the top to prefer the cards of some Servants over others. Servant Priority comes before card priority.

![Servant priority on](https://i.imgur.com/KSXE36s.png)

Then drag the team positions into the order you want. The positions are the same as in the [Skill Maker](Skill-Maker#positions).

![Servant priority](https://i.imgur.com/fz9BBtX.png)

## Brave Chains

- **Don't care:** pick cards only by priority.
- **With NP:** prefer cards of the Servant whose NP is used, to make a Brave Chain.
- **Avoid:** avoid Brave Chains.

![Battle Configs - Card Priority | Brave Chain options](https://i.imgur.com/nhPPIM0.jpeg "Battle Configs - Card Priority | Brave Chain options")

## Rearrange Cards

In FGO, the position of a card changes its effect, and the first card gives a bonus to all three. For example, a Buster card first boosts the damage of all cards.

With Rearrange Cards, FGA plays the strongest card first and the 2nd strongest card last.
It only rearranges when at most one NP and no [cards before NP](Skill-Maker#noble-phantasms) are used that turn.

![Face Cards](img/face-cards.jpg)

With the default card priority, FGA picks these 3 cards in this order:
1. Jeanne Archer Buster (strongest card)
2. Castoria Buster (3rd strongest card)
3. Jeanne Archer Arts (2nd strongest card)

Without Rearrange Cards, the Arts card would be 2nd and the Castoria card last.

## Face-card checks

Servant Priority, Brave Chains and [Shuffle cards](Battle#shuffle-cards) need to know which card belongs to which Servant. For this, FGA opens each Servant's details once when they enter the field, to see their face.
`Skip servant face-card checks` in [More Options](More-Options#battle) turns this off, but then those features don't work.
