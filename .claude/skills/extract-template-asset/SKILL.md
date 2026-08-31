---
name: extract-template-asset
description: Cut a 720p template image for app/src/main/assets out of a game screenshot, using a Region from scripts/.../locations. Use when adding or fixing a template for a server (En, Jp, Cn, Tw, Kr) - "add a Kr asset", "the image isn't detected on TW", "make a template for X" - or when checking that an existing Region actually covers what it should.
---

# Extracting a template asset from a screenshot

Read [`docs/agents/coordinates.md`](../../../docs/agents/coordinates.md) first for the two
coordinate systems. This skill is the mechanical procedure on top of it.

`crop_template.py` (next to this file, needs Pillow) reproduces `FgoGameAreaManager` and
`RealScale`, so a `Region` copied out of `locations/*.kt` resolves to the pixels the script
actually searches. Do the arithmetic with it rather than by hand - the game-area rules are
where this goes wrong.

## Before cropping

1. **Find the Region** the script searches, in `scripts/.../locations/*.kt`. Note the
   `xFromRight()` / `xFromCenter()` / `yFromBottom()` suffix and whether it sits behind an
   `if (isWide)` branch - screenshots wider than 18:9 usually take a different Region.
2. **Find the caller** in `scripts/.../modules/`. Two things change what you cut:
   - Matching is grayscale unless the `find` is inside `useColor { }`.
   - Some callers click relative to the *match*, not a fixed location -
     `Caster.openCommandSpellMenu` uses `Location(match.region.x, 150)`, so the template's
     left edge decides where the tap lands.
3. **Get the screenshot** from `docs/reference-images/<Server>/`, and put any new one there
   so the template can be re-cut later - see
   [`docs/agents/servers-and-assets.md`](../../../docs/agents/servers-and-assets.md).
   Those are stored at 720p; commit a contributed screenshot downscaled, not raw. The script
   downscales whatever it is given, so it works either way.
4. **Look at the existing assets** for the same `Images` entry. Match their convention;
   they are usually a tight crop of the localized label, not the icon. `En` is the
   fallback, so only add a server file when the art genuinely differs.

## Cropping

Source screenshots live in `docs/reference-images/<Server>/`. Stage 1 - see what the region
covers:

```bash
python3 .claude/skills/extract-template-asset/crop_template.py \
    docs/reference-images/Kr/battle-screen.png \
    --region -590,180,190,120 --anchor right --out-dir /tmp/cs
```

This writes `*-region.png` (the search region at 720p) and `*-region-grid.png` (the same
thing upscaled with a labelled 5px grid). If the region does not sit on the target, the
Region is wrong or you picked the wrong `isWide` branch - fix that before going further.

Stage 2 - read the template box off the grid image and cut it:

```bash
python3 .claude/skills/extract-template-asset/crop_template.py \
    docs/reference-images/Kr/battle-screen.png \
    --region -590,180,190,120 --anchor right \
    --template 48,31,21,11 --click-y 150 \
    --out app/src/main/assets/Kr/command_spell.png
```

`--template` is in pixels relative to the region crop's top-left. `--click-y` marks where a
match-anchored click would land on the verification image.

## Choosing the box

**Only include pixels that are part of the UI.** The battle scene behind a HUD element
changes every quest - the same element sits on a night sky on one server's screenshot and
on green foliage on another. Any background you include is noise that will not match. This
is why the existing templates are small and sit inside glyph strokes.

Judge coverage by eye on the grid image. Automated "how much of this box is glyph" scoring
is unreliable, because dark scenery reads the same as a dark outline.

Prefer a box that is entirely inside dense artwork - the filled part of a character, a
solid bar - over a larger box that reaches into the scene at its corners.

## Checking the result

Open `*-verify.png`. The yellow box is the search region, green is the template, red is the
click. Confirm the region sits on the target, the template is all UI, and the click lands on
something tappable. Also confirm the written file is 720p-scale: a template cut this way is
half the Region's script dimensions, so `Region(..., 190, 120)` can hold at most 95x60.