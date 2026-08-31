---
paths: "scripts/src/main/java/**/locations/*.kt,app/src/main/assets/**,libautomata/**"
description: The 720p/1440p coordinate systems used by image matching and Location/Region values.
---

Image matching runs at 720p, coordinates are expressed at 1440p, and the X origin depends on the
device aspect ratio — guessing produces values that are wrong on most devices.

Read [`docs/agents/coordinates.md`](../../docs/agents/coordinates.md).
