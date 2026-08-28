# Game servers, assets and localization

`app/src/main/assets/{En,Jp,Cn,Tw,Kr}/` hold per-server template images, named by `Images`
enum entries (`scripts/.../Images.kt`). `ImageLoader` looks up the current server's folder
and **falls back to `En`** when the file is absent, so only add a server-specific copy when
the art actually differs. `IFgoAutomataApi.findImage` additionally tries the `En` image on
JP to support TranslateFGO. `assets/Support/` holds user-provided servant/CE images;
`assets/tessdata/` the OCR data. Template images must be 720p — see the coordinates guide.

`app/src/main/res/values/localized.xml` holds the English source of every translatable
string — add and edit them there, in the source tree. `values/strings.xml` is only for
`translatable="false"` values (app name, server names, links, language labels).

The translated `values-*/localized.xml` files are synced from POEditor, so translations are
normally *not* edited in-repo; touch them only as a deliberate exception. The
`MissingTranslation` lint errors that follow are the expected steady state.

## The POEditor sync

POEditor exchanges strings with the repo through the dedicated **`translations` branch** —
never with `master` directly. Only the GitHub user **reconman** can trigger an import or an
export from the POEditor website.

Both directions follow the same rules:

1. Bring `translations` up to date with `master` first, so POEditor sees the current source
   strings and the export cannot revert unrelated changes.
2. Trigger the import (source strings from the repo into POEditor) or the export
   (translations from POEditor back into the repo) on the POEditor website.
3. **Review every export for lost translations before merging** — the export is lossy in
   practice. Pay particular attention to `<string-array>` entries, which are the most
   frequent casualty.
