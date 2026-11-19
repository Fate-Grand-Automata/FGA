# Game data extracted from the APKs

Files extracted from each server's APK live in the private repo
`Fate-Grand-Automata/FGO-game-data`, cloned next to this one as `../FGO-game-data`. They
are the game's copyrighted files, including commercial fonts, so they stay out of this
public repo. They let you check a server's art and text without a screenshot from it.

If `../FGO-game-data` is missing, clone it with
`gh repo clone Fate-Grand-Automata/FGO-game-data ../FGO-game-data`. Without access, ask a
maintainer.

Each server has a folder `../FGO-game-data/{En,Jp,Cn,Tw,Kr}/`:

| File | Use it to |
| --- | --- |
| `source.txt` | Tell whether the files are older than the server's current release |
| `CombineAtlas.png` | Check whether a server's icon or artwork matches an existing template (`find-apk-sprite` skill) |
| `strings.json` | Look up a label's exact text, and find the same label on another server by its key |
| `FGO-Main-Font.*` | Render a template of a text label no screenshot shows (`render-template-asset` skill) |

Refreshing the files from a newer APK, and the extraction scripts for it, are documented in
that repo's `AGENTS.md`. Changes to the data are committed there, not here.
