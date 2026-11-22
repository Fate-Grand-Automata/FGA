# Build configuration and dependency bumps

## Toolchain pins

- **JDK 21** is pinned as a Java toolchain for all modules in the root `subprojects` block,
  so it doesn't matter which JVM launches Gradle (Studio's JBR, a system JDK, CI's temurin
  21). Don't accept Android Studio's offer to generate `gradle/gradle-daemon-jvm.properties`
  (`updateDaemonJvm`): the toolchain is the pin, and that file forces a ~525 MB JBR download
  that drifts from the CI JDK.
- **Emitted bytecode stays at Java 11** in every module — from `jvmTarget` in the root
  `subprojects` block for `libautomata`/`scripts`, derived by AGP from `compileOptions` for
  `app`/`prefs`. Toolchain and bytecode target are separate knobs; raising the latter means
  checking D8 desugaring against minSdk 24. The one exception is `:scripts`'s *test*
  compilation, pinned to 17 in `scripts/build.gradle.kts` because JUnit 6 requires it — test
  code runs on the toolchain JVM and never reaches Android.
- **AGP 9 compiles Kotlin itself.** `app` and `prefs` must *not* apply
  `org.jetbrains.kotlin.android` — AGP 9 rejects it outright. Their Kotlin output lands in
  `build/intermediates/built_in_kotlinc/`, not `build/tmp/kotlin-classes/`, and the root
  block's `tasks.withType<KotlinCompile>` no longer reaches them: per-module compiler
  settings belong in that module's `android { kotlin { compilerOptions { } } }`.
- `compileSdk` is **37** while `targetSdk` stays **36** — recent androidx libraries force
  the compile level, but nothing opts into new runtime behavior. Keep the two decoupled;
  bumping `targetSdk` is a separate, user-visible decision.
- Build types: `debug`, `release`, and `ci` (`initWith(release)`, debug-signed, ARM-only
  ABIs). The `ci` type must exist in every Android module (`app`, `prefs`).
- CI's Gradle cache comes from `gradle/actions/setup-gradle`, not `setup-java`'s
  `cache: 'gradle'` — don't re-add the latter, it would cache the same directories twice
  with worse invalidation.
- `release` builds need `app/fgautomata.keystore` (GPG-decrypted in CI) and `KEYSTORE_PASS`.
  Play Store deploys are CI-only, through `fastlane` (`fastlane/Fastfile`, lanes `deploy`
  and `download_apk`).

## Bump order

Renovate runs monthly for Gradle and opens one PR per bump (Kotlin/KSP grouped), but the
Android toolchain bumps are interlocked, so a `renovate::minor` label can hide an AGP
migration. The constraints run in one direction —
bump along it and most failures never happen:

```
AGP + compileSdk ──▶ Gradle
       │
       ├──▶ Kotlin + KSP + Compose compiler ──▶ Hilt
       │
       └──▶ androidx + Compose BOM
```

- **AGP and `compileSdk` lead.** androidx AARs declare a minimum AGP *and* a minimum
  `compileSdk` in their metadata, so both usually have to move before any androidx bump
  resolves at all.
- **Gradle follows AGP, never leads it.** Gradle removes internal APIs the installed AGP
  still calls, so a wrapper bump ahead of AGP fails at plugin-apply time; AGP separately
  declares a *minimum* Gradle. Move them as a pair, AGP in front.
- **Kotlin, KSP and the Compose compiler move as one unit**, and after AGP — AGP compiles
  Kotlin itself.
- **Hilt is pinned from both sides:** it declares a minimum AGP, and its bundled
  `kotlin-metadata-jvm` caps the Kotlin metadata version it can read. So Hilt cannot lag
  Kotlin, and neither can precede AGP.
- **androidx and the Compose BOM come last**, when what's left is source-level API churn
  rather than resolution failures.
- **Test-only dependencies sit outside the chain.** They resolve against the *test*
  compilation's `jvmTarget` (Gradle's `org.gradle.jvm.version` attribute), not the app's, so
  they can outrun it — but Kotlin aborts unless `compileTestKotlin` and `compileTestJava`
  agree on that target, even with no Java test sources.

The chain can also be dragged forward **transitively**: any dependency pulling a newer
`kotlin-stdlib` re-triggers the Kotlin/Hilt constraint from a PR that looked unrelated.
`dependencyInsight` on `kotlin-stdlib` names the culprit.

When a bump fails, read the `checkAarMetadata` output first — it names the required AGP
version and compileSdk directly, much faster than bisecting versions.

Dependabot PRs here only touch `Gemfile.lock` (fastlane) and are often already behind
`master`; check before applying one.
