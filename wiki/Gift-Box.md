Receives EXP cards (embers) from the Present Box: all Silver embers, and Gold embers up to the limits you set.

## Starting

Open the Present Box, filter it to EXP cards, and press PLAY.
The [Lottery](Lottery) script can also run it when the Present Box gets full.

![Lottery](https://cdn.discordapp.com/attachments/1117932403571294228/1137078014228824134/Screenshot_20230803-042802.png "Lottery UI - with Gift Box")
![Gift Box](https://cdn.discordapp.com/attachments/1117932403571294228/1137078258874204331/Screenshot_20230803-042836.png "Lottery UI")

## Options

- **Max Gold ember set size:** the largest stack of Gold embers to receive. For example, at 10 FGA receives Gold stacks of x10 or less. At 0 it receives no Gold embers, only Silver ones.
- **Max Gold ember total count:** stop after receiving this many Gold embers.

FGA reads the stack size of Gold embers with text recognition. Stacks it can't read are skipped.

## When it stops

- **Picked X EXP stacks, resulting in a total amount of Y Gold embers:** nothing more to receive, or the Gold limit was reached.
- **Couldn't find Embers on screen:** the Present Box isn't filtered to EXP cards.
