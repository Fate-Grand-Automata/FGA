# Agent topic guides

The path-specific half of this repo's AI-agent guidance; the always-loaded half is
[`AGENTS.md`](../../AGENTS.md) at the repo root. These files are the single source of
truth — keep them provider-neutral (plain prose, no frontmatter, no tool names, no
`@`-imports) so any agent can read them, and never copy their prose elsewhere.

Three layers point at them, so an agent only pulls in a topic when it is relevant:

| Layer | Files | Mechanism |
| --- | --- | --- |
| Universal | `AGENTS.md` router section | Every agent reads the root file; each entry names a trigger and the guide to read. |
| GitHub Copilot | `.github/instructions/*.instructions.md` | `applyTo:` glob, applied when a matching file is in context. |
| Claude Code | `.claude/rules/*.md` | `paths:` glob, loaded when the model touches a matching file. |

The vendor files are stubs: a glob, a one-line reason, a pointer here. Adding a guide means
a stub in each vendor directory and a trigger line in the root router; adding an agent
(Cursor's `.cursor/rules/*.mdc`, Windsurf's `.windsurf/rules/`) means copying a stub and
renaming the glob key.
