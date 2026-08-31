---
paths: "scripts/**,app/src/main/java/io/github/fate_grand_automata/di/**,app/src/main/java/io/github/fate_grand_automata/runner/**"
description: How a script run is wired through Hilt, and the patterns automation logic follows.
---

Every script run gets its own Hilt component, and `script()` never returns normally — it exits
by exception.

Read [`docs/agents/automation-scripts.md`](../../docs/agents/automation-scripts.md).
