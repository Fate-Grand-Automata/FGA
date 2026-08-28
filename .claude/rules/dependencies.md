---
paths: "gradle/libs.versions.toml,gradle/wrapper/**,**/build.gradle.kts,settings.gradle.kts,Gemfile,Gemfile.lock,.github/workflows/**"
description: Toolchain pins (JDK/bytecode/AGP/compileSdk) and the order dependency bumps must follow.
---

The AGP, Gradle, Kotlin and androidx bumps are interlocked and only succeed in one order; the
JDK, bytecode-target and `compileSdk` pins are deliberate.

Read [`docs/agents/dependencies.md`](../../docs/agents/dependencies.md).
