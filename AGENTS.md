# AGENTS.md

Path-specific guidance lives in [`docs/agents/`](docs/agents/) so it is loaded only when relevant: read a guide when its trigger below matches. Copilot and Claude Code pick the same guides up automatically via stubs in `.github/instructions/` and `.claude/rules/` — see [`docs/agents/README.md`](docs/agents/README.md).

## What this is

Fate/Grand Automata (FGA) — an Android app that automates farming in the game *Fate/Grand Order*. It does **not** touch the game process: it reads the screen via MediaProjection, matches templates with OpenCV (plus Tesseract OCR), and taps/swipes through an AccessibilityService. Android 7+ (minSdk 24), no root required (root screenshots are an optional path).

## Topic guides — read before touching these paths

- **Any on-screen coordinate, any file in `scripts/.../locations/`, any template image added to `app/src/main/assets/`** — [`docs/agents/coordinates.md`](docs/agents/coordinates.md). Matching runs at 720p, coordinates are 1440p, and the X origin depends on aspect ratio.
- **`gradle/libs.versions.toml`, the Gradle wrapper, any `build.gradle.kts`** — [`docs/agents/dependencies.md`](docs/agents/dependencies.md). The AGP/Gradle/Kotlin/androidx bumps only succeed in one order; it also holds the JDK, bytecode-target and `compileSdk` pins.
- **Automation logic in `scripts/`, or the Hilt wiring in `app/di/` and `app/runner/`** — [`docs/agents/automation-scripts.md`](docs/agents/automation-scripts.md). Every run gets its own Hilt component, and `script()` never returns normally.
- **Assets or strings under `app/src/main/res/`** — [`docs/agents/assets-and-i18n.md`](docs/agents/assets-and-i18n.md). English strings live in `values/localized.xml`; the translated `values-*` copies are synced from POEditor.

## Build & test

```bash
./gradlew :app:compileDebugKotlin # Compiles the kotlin code for the android app.
./gradlew :scripts:test          # the only unit tests in the repo (JUnit 5 + assertk + mockk)
./gradlew :scripts:test --tests '*SupportSelectionTest'          # single test class
./gradlew :scripts:test --tests '*SupportSelectionTest.someName' # single test method
./gradlew lint                   # abortOnError = false, so lint never fails the build
./gradlew dependencyUpdates      # ben-manes versions plugin
```

- Everything builds on **JDK 21** via a Gradle toolchain pin; emitted bytecode stays at **Java 11**.
- `lint` reports `MissingTranslation` **errors** on `app`. That is the expected steady state, not a regression — don't try to fix them in-repo.

## Module layout and dependency direction

```
app  ──▶ scripts ──▶ libautomata
 └──▶ prefs ──▶ scripts
```

- **`libautomata/`** — pure JVM Kotlin (no Android deps). Game-agnostic automation primitives: `Region`, `Location`, `Size`, `Pattern`, `AutomataApi`/`StandardAutomataApi`, `ScreenshotManager`, `ImageMatcher`, `Clicker`, `Swiper`, `Waiter`, `Scale`, `GameAreaManager`, `EntryPoint`, `ExitManager`. Everything platform-specific is an interface (`ScreenshotService`, `GestureService`, `OcrService`, `PlatformImpl`, `PlatformPrefs`) implemented in `app`.
- **`scripts/`** — pure JVM Kotlin. All FGO knowledge: entry points, per-screen modules, coordinates, image names, prefs *interfaces*. This is where automation logic belongs, and the only module with tests (it's testable precisely because it has no Android dependency — keep it that way).
- **`prefs/`** — Android library implementing the `I*Preferences` interfaces from `scripts` on top of `flow-preferences`. `PrefsCore` holds the raw `Pref<T>` delegates; `PreferencesImpl` adapts them to the script-facing interfaces.
- **`app/`** — Compose UI, Hilt wiring, the two services, and the Android implementations of the `libautomata` interfaces (`imaging/`, `accessibility/`, `root/`, `util/`).

## Repo conventions

- Kotlin official code style; `kotlin.time.Duration` is used everywhere for waits.
- Hilt + KSP for DI; Compose (BOM-managed) with several `ExperimentalX` opt-ins already enabled in `app/build.gradle.kts`.
- Dependencies live in `gradle/libs.versions.toml` — always add/bump there, never inline coordinates.
- `wiki/` is the source of the GitHub wiki (published by `.github/workflows/publish-wiki.yml`) — user-facing docs go there, not in the README.
- Agent guidance: keep this file to what's true repo-wide and put path-specific rules in [`docs/agents/`](docs/agents/), following [`docs/agents/README.md`](docs/agents/README.md).
