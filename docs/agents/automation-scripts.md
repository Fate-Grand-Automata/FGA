# Writing automation logic

## How a script run is wired (Hilt)

Two long-lived services: `ScriptRunnerService` (foreground service owning the overlay,
notification, and `ScriptManager`) and `TapperService` (AccessibilityService providing
gestures).

Each *run* gets its own Hilt component: `ScriptComponent` (`@ScriptScope`, child of
`ServiceComponent`, built via `ScriptComponentBuilder` with the live `ScreenshotService`
bound in). `ScriptManager` builds it, pulls the chosen `EntryPoint` out through the
`ScriptEntryPoint` Hilt entry point, runs it on its own thread, and tears the component
down at exit (including explicitly closing the Tesseract `OcrService`). Consequences:

- `ScriptScope` singletons (`Locations`, `ScriptAreaTransforms`, `ScreenshotManager`,
  `FgoAutomataApi`, …) are recreated per run, so screen-geometry values are computed once
  per run and cached.
- Adding a new script mode means: entry point class in `scripts/.../entrypoints/`, a getter
  on `ScriptEntryPoint`, a `ScriptModeEnum` value, and a branch in
  `ScriptManager.getEntryPoint`.
- Bindings for a run live in `app/di/script/` (`LibAutomataModule` for `libautomata`
  interfaces, `ScriptsModule` for FGO-level ones).

## Script structure

Entry points extend `EntryPoint` and implement `script(): Nothing`. `ExitManager` implements
stop/pause by throwing `ScriptAbortException` from inside wait/click primitives, which is why
`script()` never returns normally — exit and errors surface through `scriptExitListener`.

The dominant pattern (see `AutoBattle.loop()`) is a **map of screen detectors to
handlers**, evaluated in order inside `useSameSnapIn { }`, one action per iteration, then a
short wait. Order matters: earlier entries win. Detectors must be cheap image/color checks,
not stateful logic.

Mix in `IFgoAutomataApi` (delegating to an injected `FgoAutomataApi`) to get `prefs`,
`images`, `locations`, `messages`, plus the `Region`/`Location` extension DSL:
`images[Images.Menu] in someRegion`, `region.exists(pattern, timeout)`,
`region.waitVanish(...)`, `location.click()`, `duration.wait()`.

- **`useSameSnapIn { }`** caches one screenshot for the whole block. Any group of checks
  that must agree on a single frame has to be inside it; without it every `exists` takes a
  fresh screenshot (slow, and racy across animations).
- **User-facing text** never goes in `scripts/` (no Android `R`). Emit a
  `ScriptNotify`/`ScriptLog` through `messages: IScriptMessages`;
  `app/util/ScriptMessages.kt` maps it to a localized string, toast, or notification.
- **Never hardcode wait/similarity magic numbers casually** — `PlatformPrefs` exposes
  user-tunable `minSimilarity`, `waitMultiplier`, `swipeMultiplier` (the Fine-Tune screen).
  Explicit `similarity` arguments in `Region.exists` should be rare; anything under ~0.65
  means the template image is wrong.
