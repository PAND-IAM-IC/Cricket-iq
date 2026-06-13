# Git Essentials (practical cheatsheet)

## The mental model — work flows through 3 places

```
Working directory   →   Staging area   →   Local repo   →   Remote (GitHub)
(your real files)       (git add)           (git commit)     (git push)
```

- **Working directory** — the actual files you edit.
- **Staging area (the "index")** — a loading dock where you pick *what* goes into the next snapshot. `git add` puts things here.
- **Local repo** — your commit history, stored in the hidden `.git` folder. `git commit` saves a snapshot.
- **Remote** — the GitHub copy. `git push` uploads, `git pull` downloads.

Why the staging step exists: it lets you commit *some* of your changes, not all, so each
commit is a clean, logical unit. (Common interview question — now you can answer it.)

---

## Everyday commands

```bash
git status                 # what's changed, what's staged — run this CONSTANTLY
git add <file>             # stage one file
git add .                  # stage everything (not gitignored)
git restore --staged <f>   # un-stage a file (take it back off the dock)

git commit -m "message"    # snapshot the staged changes into local history
git log --oneline          # compact history of commits
git log --oneline -5       # last 5 commits

git diff                   # changes NOT yet staged
git diff --staged          # changes that ARE staged (about to be committed)

git push                   # upload local commits to GitHub
git pull                   # download + merge remote commits
git remote -v              # show where 'origin' points (check the alias!)
```

---

## Branches (you'll use these for the 🟡 learning experiments)

```bash
git branch                       # list branches, * marks current
git switch -c feature/jwt-auth   # create AND switch to a new branch
git switch main                  # switch back to main
git merge feature/jwt-auth       # merge a branch into your current branch
git branch -d feature/jwt-auth   # delete a branch after merging
```

Use a branch per feature or experiment (e.g. `experiment/lazy-circular-dep`), so `main`
always stays in a working state. Merge when it's solid.

---

## Undo cheats (handle with care)

```bash
git restore <file>         # discard unstaged changes to a file (can't undo!)
git restore --staged <f>   # unstage but keep your edits
git commit --amend         # fix the LAST commit's message/content (before pushing)
git reset --soft HEAD~1    # undo last commit, keep changes staged
git revert <commit>        # make a NEW commit that undoes an old one (safe for shared history)
```

Rule of thumb: `revert` is safe (adds history); `reset`/`restore` can destroy work — only
use on commits you haven't pushed/shared.

---

## This project's normal loop

```bash
# 1. make some changes in your editor, then:
git status                 # see what changed
git add .                  # stage it
git status                 # confirm what's staged
git commit -m "Add player entity and repository"
git push                   # send to GitHub
```

Commit small and often, with messages that say *why*. A good message:
`"Add JWT filter to validate tokens on protected endpoints"` — not `"stuff"`.

---

## Good commit message style

- Present tense, imperative: "Add", "Fix", "Refactor" (not "Added", "Fixing").
- Short summary line (< ~70 chars). Add a blank line + details if needed.
- One logical change per commit.
