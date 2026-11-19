---
paths: ".github/workflows/**"
description: Changing the actions a workflow uses must regenerate the actions lockfile.
---

Adding, removing or changing a `uses:` line, or adding, removing or renaming a workflow, must be
committed with a regenerated `.github/workflows/actions.lock` (`gh actions-lock`). Otherwise its
runs are blocked by the repository's *Require lockfile* policy.

Read [`docs/agents/github-workflows.md`](../../docs/agents/github-workflows.md).
