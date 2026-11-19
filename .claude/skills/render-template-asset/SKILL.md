---
name: render-template-asset
description: Render a 720p template image of a UI text label from FGO's own font and string tables, for a server with no screenshot of that label. Use when a template is missing for a server (En, Jp, Cn, Tw, Kr) and users can't easily reproduce the screen - e.g. "we have no CN screenshot of the QP warning", "can we get the text from the APK?" - or to look up a label's exact text and color in the game files. Needs the private ../FGO-game-data repo.
---

# Rendering a template asset from the game's font

Use this only when no screenshot of the label exists. A template cut from a screenshot with
[`extract-template-asset`](../extract-template-asset/SKILL.md) is the default, since it holds
the real outline and shading. Read [`docs/agents/coordinates.md`](../../../docs/agents/coordinates.md)
first, and find the Region and its caller as described in that skill.

## Getting the font and the text

Both are in the private repo `../FGO-game-data`. If it's missing, tell the user and stop;
[`docs/agents/game-data.md`](../../../docs/agents/game-data.md) says how to get it.

Each server's folder `../FGO-game-data/<Server>/` has the UI font `FGO-Main-Font.*`, which
differs per server except that EN and JP share one, and the UI strings in `strings.json`.
Find a label's key by searching for text you can read on any screenshot, then look the same
key up in the target server's file. To refresh them from a newer APK, follow
`../FGO-game-data/AGENTS.md`.

- `[RRGGBB]` at the start of a string is the label's color; leave it out of `--text`.

## Rendering

`render_template.py` (next to this file; needs Pillow, numpy and opencv-python-headless)
renders the text at 4x, shrinks it to 720p the way the app shrinks a screenshot, and crops it
to the glyphs. `--check` scores it with the app's pipeline at half-pixel shifts.

```bash
python3 .claude/skills/render-template-asset/render_template.py \
    --font ../FGO-game-data/Kr/FGO-Main-Font.otf --text "QP가 부" --color DB0052 \
    --region -500,222,397,47 --anchor center \
    --check docs/reference-images/Kr/skill-enhancement/skill-enhancement-insufficient-qp.png \
    --check docs/reference-images/Kr/skill-enhancement/skill-enhancement-mixed-levels.png \
    --out app/src/main/assets/Kr/skill_insufficient_qp.png
```

- **Size:** use `--fit` against a screenshot of the same label, or of another label in the
  same place, on any server. Labels keep their size across servers. The Skill Enhancement
  warnings are size 25.
- **Text:** when two labels in one region share words (CN's "所需道具不足" and "量子不足!"),
  render only enough of the text to tell them apart.
- **Template height:** it must fit the `isWide` variant of the Region too, which is often
  shorter.

## Checking the result

Before relying on a server's font, render a label on that server that you have a screenshot
of, and confirm it scores like the rendered labels below.

Rendering fails when the text color and its background turn into nearly the same gray:

| Label | Gray of text / background | Rendered score |
|---|---|---|
| KR warning, sans font on a dark bar | 64 / 15 | 0.82-0.89 |
| CN prompt, cyan on a teal bar | 155 / 49 | 0.86-0.93 |
| TW warning, serif font on a teal bar | 72 / 67 | below 0.8 at some shift, with or without an outline |

On such labels the dark outline carries the shape, and a rendered outline doesn't reproduce
it. Check the grays before trusting a render there; a template that never matches is no
better than a missing one.
