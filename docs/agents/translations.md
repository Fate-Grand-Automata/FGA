# Localization

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

## Default support images carry translated names

`app/src/main/assets/Support/servant/<Name>/` and `app/src/main/assets/Support/ce/<Name>.png`
ship the default servants and craft essences. Those English names *are* the identity of a
support entry — they are the keys written to preferences and the names matched against the
extracted image files — so never translate them in place. The UI shows a translated label
instead, resolved through `SupportNameResources`
(`app/src/main/java/io/github/fate_grand_automata/util/SupportNameResources.kt`), which maps
each servant folder name and each CE file name (minus the extension) to a string resource.

Adding, renaming or removing anything under `assets/Support` therefore takes three edits:

1. the asset — a folder under `servant/`, or a `.png` under `ce/`;
2. a `servant_name_*` / `ce_name_*` entry in `values/localized.xml`;
3. the matching line in `servantNameResIds` or `ceNameResIds`.

A missing entry fails silently: the lookup falls back to the raw English name, which is also
how user-added custom supports are meant to display. A rename that skips step 3 breaks the
mapping the same quiet way, so the existing translation just stops being used. New strings
reach the other languages through the POEditor sync above like any other source string.
