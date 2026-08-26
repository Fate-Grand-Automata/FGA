# AGENTS.md

Guidance for AI coding agents working in this repository. It follows the [AGENTS.md](https://agents.md/) convention, so any agent that supports it picks this file up directly; `CLAUDE.md` just includes it.

## What this is

Fate/Grand Automata (FGA) — an Android app that automates farming in the game *Fate/Grand Order*. It does **not** touch the game process: it reads the screen via MediaProjection, matches templates with OpenCV (plus Tesseract OCR), and taps/swipes through an AccessibilityService. Android 7+ (minSdk 24), no root required (root screenshots are an optional path).

## Build & test

```bash
./gradlew assembleDebug          # debug APK, applicationId suffix .test, signed with committed app/fgadebug.keystore
./gradlew assembleCi             # minified release-like APK signed with the debug keystore (what PR CI builds)
./gradlew :scripts:test          # the only unit tests in the repo (JUnit 5 + assertk + mockk)
./gradlew :scripts:test --tests '*SupportSelectionTest'          # single test class
./gradlew :scripts:test --tests '*SupportSelectionTest.someName' # single test method
./gradlew lint                   # abortOnError = false, so lint never fails the build
./gradlew dependencyUpdates      # ben-manes versions plugin
```

- Everything builds on **JDK 21**: the root `subprojects` block pins a Java toolchain of 21 for all modules, so it doesn't matter which JVM launches Gradle (Studio's JBR, a system JDK, CI's temurin 21).
- **Emitted bytecode stays at Java 11** in every module. For `libautomata`/`scripts` that comes from `jvmTarget` in the root `subprojects` block; for `app`/`prefs` AGP derives it from their `compileOptions`. The toolchain and the bytecode target are separate knobs; raising the latter means checking D8 desugaring against minSdk 24.
- **AGP 9 compiles Kotlin itself.** `app` and `prefs` must *not* apply `org.jetbrains.kotlin.android` — AGP 9 rejects it outright. Their Kotlin output lands in `build/intermediates/built_in_kotlinc/`, not `build/tmp/kotlin-classes/`, and the root block's `tasks.withType<KotlinCompile>` no longer reaches them: per-module compiler settings belong in that module's `android { kotlin { compilerOptions { } } }`.
- `compileSdk` is **37** while `targetSdk` stays **36** — recent androidx libraries force the compile level, but nothing opts into new runtime behavior. Keep the two decoupled; bumping `targetSdk` is a separate, user-visible decision.
- Android Studio periodically offers to generate `gradle/gradle-daemon-jvm.properties` (`updateDaemonJvm`). Don't accept it — the toolchain above is the pin, and that file instead forces a ~525 MB JBR download that drifts from the CI JDK.
- Dependencies live in `gradle/libs.versions.toml` — always add/bump there, never inline coordinates.
- `versionCode`/`versionName` come from the `FGA_VERSION_CODE`/`FGA_VERSION_NAME` env vars (default 1 / "0.1.0"). When installing a debug build over a store install, set `FGA_VERSION_CODE` to at least the installed version — Android refuses downgrades.
- `release` builds need `app/fgautomata.keystore` (GPG-decrypted in CI) and `KEYSTORE_PASS`. Release/Play Store deploys go through `fastlane` (`fastlane/Fastfile`, lanes `deploy` and `download_apk`) and are CI-only.
- Build types: `debug`, `release`, and `ci` (`initWith(release)`, debug-signed, ARM-only ABIs). The `ci` type must exist in every Android module (`app`, `prefs`).

## Bumping dependencies

Renovate opens one PR per bump, but the Android ones are interlocked, so a `renovate::minor` label can hide an AGP migration. Constraints found while collecting the 2026-08 batch:

- Gradle **>= 9.6** requires AGP 9 — 9.6 removed `InternalProblems`, which AGP 8.x used.
- Hilt **>= 2.59** refuses AGP < 9, and Hilt <= 2.58 cannot read Kotlin 2.4 metadata. Hilt and Kotlin therefore have to move together.
- `core-ktx` 1.19, `lifecycle` 2.11 and `hilt-navigation-compose` 1.4 require **AGP 9.1 + compileSdk 37**.
- Coil 3.5 pulls `kotlin-stdlib` 2.4, which drags the Hilt/Kotlin constraint above in with it.
- AGP 8.13.2 is the last 8.x, so none of the above can be satisfied by staying on AGP 8.

When a bump fails, read the `checkAarMetadata` output first — it names the required AGP version and compileSdk directly, which is much faster than bisecting versions. Dependabot PRs here only touch `Gemfile.lock` (fastlane) and are often already behind `master`; check before applying one.

## Module layout and dependency direction

```
app  ──▶ scripts ──▶ libautomata
 └──▶ prefs ──▶ scripts
```

- **`libautomata/`** — pure JVM Kotlin (no Android deps). Game-agnostic automation primitives: `Region`, `Location`, `Size`, `Pattern`, `AutomataApi`/`StandardAutomataApi`, `ScreenshotManager`, `ImageMatcher`, `Clicker`, `Swiper`, `Waiter`, `Scale`, `GameAreaManager`, `EntryPoint`, `ExitManager`. Everything platform-specific is an interface (`ScreenshotService`, `GestureService`, `OcrService`, `PlatformImpl`, `PlatformPrefs`) implemented in `app`.
- **`scripts/`** — pure JVM Kotlin. All FGO knowledge: entry points, per-screen modules, coordinates, image names, prefs *interfaces*. This is where automation logic belongs, and the only module with tests (it's testable precisely because it has no Android dependency — keep it that way).
- **`prefs/`** — Android library implementing the `I*Preferences` interfaces from `scripts` on top of `flow-preferences`. `PrefsCore` holds the raw `Pref<T>` delegates; `PreferencesImpl` adapts them to the script-facing interfaces.
- **`app/`** — Compose UI, Hilt wiring, the two services, and the Android implementations of the `libautomata` interfaces (`imaging/`, `accessibility/`, `root/`, `util/`).

## How a script run is wired (Hilt)

Two long-lived services: `ScriptRunnerService` (foreground service owning the overlay, notification, and `ScriptManager`) and `TapperService` (AccessibilityService providing gestures).

Each *run* gets its own Hilt component: `ScriptComponent` (`@ScriptScope`, child of `ServiceComponent`, built via `ScriptComponentBuilder` with the live `ScreenshotService` bound in). `ScriptManager` builds it, pulls the chosen `EntryPoint` out through the `ScriptEntryPoint` Hilt entry point, runs it on its own thread, and tears the component down at exit (including explicitly closing the Tesseract `OcrService`). Consequences worth knowing:

- `ScriptScope` singletons (`Locations`, `ScriptAreaTransforms`, `ScreenshotManager`, `FgoAutomataApi`, …) are recreated per run, so screen-geometry values are computed once per run and cached.
- Adding a new script mode means: entry point class in `scripts/.../entrypoints/`, a getter on `ScriptEntryPoint`, a `ScriptModeEnum` value, and a branch in `ScriptManager.getEntryPoint`.
- Bindings for a run live in `app/di/script/` (`LibAutomataModule` for `libautomata` interfaces, `ScriptsModule` for FGO-level ones).

## Writing automation logic

Entry points extend `EntryPoint` and implement `script(): Nothing`. `ExitManager` implements stop/pause by throwing `ScriptAbortException` from inside wait/click primitives, which is why `script()` never returns normally — exit and errors surface through `scriptExitListener`.

The dominant pattern (see `AutoBattle.loop()`) is a **map of screen detectors to handlers**, evaluated in order inside `useSameSnapIn { }`, one action per iteration, then a short wait. Order matters: earlier entries win. Detectors must be cheap image/color checks, not stateful logic.

Mix in `IFgoAutomataApi` (delegating to an injected `FgoAutomataApi`) to get `prefs`, `images`, `locations`, `messages`, plus the `Region`/`Location` extension DSL: `images[Images.Menu] in someRegion`, `region.exists(pattern, timeout)`, `region.waitVanish(...)`, `location.click()`, `duration.wait()`.

- **`useSameSnapIn { }`** caches one screenshot for the whole block. Any group of checks that must agree on a single frame has to be inside it; without it every `exists` takes a fresh screenshot (slow, and racy across animations).
- **User-facing text** never goes in `scripts/` (no Android `R`). Emit a `ScriptNotify`/`ScriptLog` through `messages: IScriptMessages`; `app/util/ScriptMessages.kt` maps it to a localized string, toast, or notification.
- **Never hardcode wait/similarity magic numbers casually** — `PlatformPrefs` exposes user-tunable `minSimilarity`, `waitMultiplier`, `swipeMultiplier` (the Fine-Tune screen). Explicit `similarity` arguments in `Region.exists` should be rare; anything under ~0.65 means the template image is wrong.

## The two coordinate systems (most common source of bugs)

`CONTRIBUTING.md` documents this in detail; the essentials:

- **Image matching happens at 720p grayscale.** Screenshots are captured at native resolution, converted to grayscale, scaled to 720p, cropped to the game area (letterbox/notch bars removed), then cropped to the search region. **Template images added to `app/src/main/assets/` must be 720p** and are grayscaled unless matched inside `useColor { }`.
- **Coordinates are expressed at 1440p** (`scriptDimension` 1440p, `compareDimension` 720p; `Scale` converts between script/screen/image space). Origin is the top-left of the *game area*, not the physical screen.
- Y ranges 0..1440; the right edge X depends on aspect ratio, so absolute X is almost always wrong. Use the `IScriptAreaTransforms` helpers — `xFromCenter()`, `xFromRight()`, `yFromBottom()` — on both `Location` and `Region`. `isWide` (wider than 18:9) selects alternate coordinates for many UI elements; follow the existing `if (isWide) … else …` style in `locations/`.
- Coordinates belong in `scripts/.../locations/*.kt` as named `val`s on the `Locations` graph, not inline in modules.

## Game servers and assets

`app/src/main/assets/{En,Jp,Cn,Tw,Kr}/` hold per-server template images, named by `Images` enum entries (`scripts/.../Images.kt`). `ImageLoader` looks up the current server's folder and **falls back to `En`** when the file is absent, so only add a server-specific copy when the art actually differs. `IFgoAutomataApi.findImage` additionally tries the `En` image on JP to support TranslateFGO. `assets/Support/` holds user-provided servant/CE images; `assets/tessdata/` the OCR data.

Localization is done externally via POEditor — edit `app/src/main/res/values/strings.xml` (English source) only; the translated `values-*` folders are synced from there. A consequence: `lint` reports ~73 `MissingTranslation` **errors** on `app`. That is the expected steady state, not a regression — don't try to fix them in-repo.

## Repo conventions

- Kotlin official code style; `kotlin.time.Duration` is used everywhere for waits.
- Hilt + KSP for DI; Compose (BOM-managed) with several `ExperimentalX` opt-ins already enabled in `app/build.gradle.kts`.
- `wiki/` is the source of the GitHub wiki (published by `.github/workflows/publish-wiki.yml`) — user-facing docs go there, not in the README.
- Renovate manages dependency bumps (Gradle monthly, grouped Kotlin/KSP).
