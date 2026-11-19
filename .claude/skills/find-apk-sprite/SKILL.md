---
name: find-apk-sprite
description: Search FGO's UI atlases, extracted from a server's APK, for the sprite behind a template image in app/src/main/assets. Use to check whether a server (En, Jp, Cn, Tw, Kr) uses the same icon or artwork as an existing template - "does CN use the same chevron?", "do we need a TW copy of this image?", "check the APK" - without a screenshot from that server. Needs the private ../FGO-game-data repo.
---

# Finding a template's sprite in the APK atlases

Icons and artwork (not text labels) come from atlas textures that ship in the APK. If a
server's atlas holds the same sprite as an existing template, that server can use the `En`
fallback; see [`docs/agents/servers-and-assets.md`](../../../docs/agents/servers-and-assets.md).
For text labels, use [`render-template-asset`](../render-template-asset/SKILL.md) instead.

The atlases are in the private repo `../FGO-game-data`. If it's missing, tell the user and
stop; [`docs/agents/game-data.md`](../../../docs/agents/game-data.md) says how to get it.

## Searching the atlases

`../FGO-game-data/<Server>/*Atlas.png` holds the exported atlases; `source.txt` next to them
names the APK version:

```bash
python3 .claude/skills/find-apk-sprite/find_sprite.py app/src/main/assets/En/rank_up.png \
    ../FGO-game-data/*/*Atlas.png --out-dir <scratchpad>/matches
```

Each line gives the best score, the scale and the box in the atlas. Check the crops in
`--out-dir` by eye.

- **Same sprite:** around 0.93, at the same scale on every server. UI sprites sit at 1.5x
  their 720p size in `CombineAtlas`.
- **Not in that atlas:** about 0.6 or lower. Try the other atlases before concluding the
  server's art differs.
- `--background` is the color the sprite sits on in game (default: the light panel of the
  NP rank-up box). A sprite with a soft glow scores lower on the wrong background.

## When no atlas has the sprite

Only the atlases built into the APK can be exported; art from downloaded asset bundles
isn't covered. To export another atlas, or a newer APK's, follow `../FGO-game-data/AGENTS.md`.
