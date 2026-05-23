---
name: github-pr-publishing
description: Use when publishing this repository's current branch to GitHub, opening, updating, or rewriting a pull request/MR, or preparing commits for review.
---

# GitHub PR Publishing

## Overview

Publish this repo's branch with deliberate scope control, Conventional Commits,
and a GitHub pull request. In this repo, "MR" means a GitHub pull request unless
the user says otherwise.

## Workflow

1. Confirm at least one PR write path works before preparing persistent PR
   artifacts: the GitHub connector can create/update pull requests for this
   repo, or `gh auth status` succeeds. If the connector returns a permission
   error, immediately fall back to `gh`. If `gh` is not authenticated, start the
   `gh auth login` flow and retry the PR operation after authentication. If
   sandboxed `gh auth status` reports a stale/invalid token but Git operations
   over SSH work or the machine is known to have GitHub CLI configured, rerun
   `gh auth status` outside the sandbox with `sandbox_permissions=require_escalated`;
   macOS keychain-backed auth can be inaccessible inside the sandbox. If the
   outside-sandbox check succeeds, run the needed `gh pr create` or `gh pr edit`
   command outside the sandbox as well.
2. Inspect `git status -sb` before staging. Keep unrelated local files out of
   the commit and use explicit paths when the worktree is mixed.
3. Use Conventional Commits for new commit messages:
   `type(optional-scope): short imperative description`.
   Prefer `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, or `ci`.
   Use `!` or a `BREAKING CHANGE:` footer for breaking changes.
4. Run the most relevant verification for the changed surface.
5. Determine the current branch and target branch. Default the target branch to
   `main` unless the user or repository metadata says otherwise.
6. Run `git log main..HEAD --oneline` to get commits on this branch. If there
   are no commits ahead of `main`, inform the user and stop.
7. Run `git diff main...HEAD --stat` and `git diff main...HEAD` to understand
   the complete pull request scope before writing the title or body.
8. Check whether an open pull request already exists for the current branch.
   Prefer the GitHub connector; use `gh pr list --head <current-branch>` when
   the connector cannot resolve branch PRs cleanly.
9. Draft the PR title and body in memory. Do not create `pr.md` or any other
   repository-local PR body file unless the user explicitly asks for one.
10. Push the current branch with tracking.
11. Create a draft GitHub PR by default, or update the existing open PR for this
    branch. Prefer the GitHub connector; use `gh pr create` or `gh pr edit`
    when connector creation or updates cannot infer the repo or branch cleanly
    or when the connector lacks write permission. If an open PR exists, update
    that PR; do not add a comment with the replacement body and do not create a
    duplicate PR. After updating, fetch or view the PR again and confirm the
    remote title/body actually changed.
12. If both PR write paths are unavailable, leave the branch pushed and report
    a GitHub compare URL with `quick_pull=1`, `title`, and `body` query
    parameters when practical. Also include the exact PR title and body to paste.
    Do not call the compare URL an opened PR or draft. Remove any temporary
    files created while attempting the update before stopping.

## PR Description

Base the title and body on the commits, diff stat, and full diff. Keep the title
under 72 characters and make it describe the outcome, not the implementation
mechanics.

The PR body must contain:

- `## Summary` with 2-4 bullets explaining what changed and why.
- `## Changes` with key modifications grouped logically.
- `## Test plan` with a checklist of verification commands and UI checks.
- A short note for any known local-only files deliberately left out, only when
  that applies.

When using the GitHub connector, pass the title and body directly. When using
`gh`, avoid shell-escaped inline Markdown; write the body to a temporary file
outside the repository and delete it before finishing:

```bash
body_file="$(mktemp -t drink-wise-pr-body.XXXXXX.md)"
printf '%s\n' "$body" > "$body_file"
gh pr create --title "$title" --body-file "$body_file" --base main --draft  # new PR
gh pr edit <number> --title "$title" --body-file "$body_file"              # existing PR
rm -f "$body_file"
```

If `gh` only works outside the sandbox, keep the same temporary-file pattern and
place the file in `/private/tmp` or another writable temp directory outside the
repository. Do not leave PR body files in the checkout.

## Safety

- Never stage IDE state, local MCP config, artifacts, or unrelated docs unless
  the user explicitly includes them.
- Never create `pr.md` or other PR draft files in the repository unless the user
  explicitly asks for a local artifact.
- Never force-push unless the user explicitly asks for it.
- Prefer draft PRs unless the user asks for ready review.
