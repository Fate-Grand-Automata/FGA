---
paths: ".github/workflows/**"
description: Workflow changes must regenerate the actions lockfile.
---

Every workflow change must be committed with a regenerated `.github/workflows/actions.lock`
(`gh actions-lock`). Otherwise its runs are blocked by the repository's *Require lockfile* policy.

Read [`docs/agents/github-workflows.md`](../../docs/agents/github-workflows.md).
