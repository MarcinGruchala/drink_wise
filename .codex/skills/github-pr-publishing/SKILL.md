---
name: github-pr-publishing
description: Use when publishing this repository's current branch to GitHub, opening or updating a pull request/MR, or preparing commits for review.
---

# GitHub PR Publishing

## Overview

Publish this repo's branch with deliberate scope control, Conventional Commits,
and a GitHub pull request. In this repo, "MR" means a GitHub pull request unless
the user says otherwise.

## Workflow

1. Inspect `git status -sb` and the relevant diff before staging.
2. Keep unrelated local files out of the commit. Use explicit paths when the
   worktree is mixed.
3. Use Conventional Commits for new commit messages:
   `type(optional-scope): short imperative description`.
   Prefer `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, or `ci`.
   Use `!` or a `BREAKING CHANGE:` footer for breaking changes.
4. Run the most relevant verification for the changed surface.
5. Push the current branch with tracking.
6. Create a draft GitHub PR by default. Prefer the GitHub connector; use `gh pr`
   only when connector creation cannot infer the repo or branch cleanly.
7. If an open PR already exists for the branch, report or update it instead of
   creating a duplicate.

## PR Shape

Use a clear title that summarizes the branch. The PR body should include:

- Summary of what changed.
- Why the change matters.
- Verification commands or UI checks run.
- Any known local-only files deliberately left out.

## Safety

- Never stage IDE state, local MCP config, artifacts, or unrelated docs unless
  the user explicitly includes them.
- Never force-push unless the user explicitly asks for it.
- Prefer draft PRs unless the user asks for ready review.
