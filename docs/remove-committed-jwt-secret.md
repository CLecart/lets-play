# Remove committed JWT secret from repository history

This document explains a safe, repeatable plan to remove an accidentally
committed JWT secret from Git history, rotate the secret in your
deployments/CI, and coordinate the forced-push with the team.

Summary

- The working tree already uses a placeholder in
  `src/main/resources/application.properties`:
  `app.jwt.secret=CHANGE_ME_DO_NOT_COMMIT`.
- The actual secret still exists in Git history and must be removed with
  a history-rewrite tool (`git-filter-repo` recommended, BFG as
  alternative).
- Rewriting history is destructive (it changes commit SHAs). Back up
  first and coordinate a maintenance window.

Prerequisites and cautions

- Back up the repository (mirror clone). Do not run the rewrite on the
  shared clone without a backup.
- All collaborators must re-clone after a forced push or follow the
  rescue steps below.
- Ensure you have rights to force-push rewritten refs to the remote.
- Prefer `git-filter-repo` (recommended): faster and more flexible than
  BFG.

Step 0 — prepare and validate (non-destructive)

1. Quick scan for obvious occurrences (if you know the secret text):

```bash
# from repository root
git grep -n "YOUR_OLD_JWT_SECRET" || true
git grep -n "app.jwt.secret" || true
```

2. Create a mirror backup for safety:

```bash
cd "$(git rev-parse --show-toplevel)"
mkdir -p ../lets-play-backups
git clone --mirror . ../lets-play-backups/lets-play-mirror.git
```

3. Replace the secret in the working tree with a placeholder and commit
   so the secret isn't reintroduced after history rewrite. Use the
   helper script `scripts/prepare_remove_jwt_secret.sh`:

```bash
bash scripts/prepare_remove_jwt_secret.sh
# inspect the diff, then commit
git add src/main/resources/application.properties
git commit -m "chore(secrets): replace committed JWT secret with placeholder"
```

Step 1 — choose a history-rewrite tool

- Recommended: `git-filter-repo` (Python). Install with:

```bash
pip install --user git-filter-repo
```

- Alternative: BFG Repo-Cleaner (Java). BFG is useful for file
  deletions and simple replacements but `git-filter-repo` is preferred.

Step 2 — run `git-filter-repo` safely (manual push required)

This script below (also provided at `scripts/rewrite_history_remove_jwt.sh`)
performs the rewrite in a fresh mirror clone and does NOT push to origin.
You must inspect the mirror and perform the final push manually when
you are ready.

High-level commands (safe flow):

```bash
# 1) Make a fresh mirror clone (do this on a machine that can push)
git clone --mirror git@github.com:your-org/lets-play.git repo-mirror.git
cd repo-mirror.git

# 2) Prepare a replace-text file (do NOT echo the literal secret to
#    public logs). The file contains lines like:
#    OLD_SECRET_TEXT==>CHANGE_ME_DO_NOT_COMMIT
printf '%s==>CHANGE_ME_DO_NOT_COMMIT
' "<PASTE_OLD_SECRET_HERE>" > replace-jwt-secret.txt
chmod 600 replace-jwt-secret.txt

# 3) Run git-filter-repo (rewrites all refs in this mirror)
git filter-repo --replace-text replace-jwt-secret.txt

# 4) Verify the old secret is gone
git grep -nF "<PASTE_OLD_SECRET_HERE>" || true

# 5) When satisfied, push rewritten refs to origin (manual, force)
# WARNING: This is destructive. Ensure backups + team coordination.
git push --force --mirror origin
```

Step 2 (alternative) — using BFG

If you prefer BFG, use it from a fresh mirror clone. Example:

```bash
git clone --mirror git@github.com:your-org/lets-play.git repo-mirror.git
cd repo-mirror.git

# Prepare replace.txt with a line like:
# OLD_SECRET_TEXT==>CHANGE_ME_DO_NOT_COMMIT
java -jar /path/to/bfg.jar --replace-text replace.txt

# Cleanup and prune
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Inspect and then push if ready
git push --force --mirror origin
```

Step 3 — post-rewrite cleanup and rotation

1. Notify team to re-clone or follow recovery steps.
2. Rotate secrets in all deployments / CI / secret stores:
   - Generate a new secret value in your secrets manager.
   - Update CI variables, platform secrets, and environments to the
     new value (`APP_JWT_SECRET`).
3. Revoke or reduce lifetime of JWTs signed with the old secret if
   possible; otherwise shorten token lifetime temporarily until all
   clients refresh.
4. Update project documentation (`README.md`, `SECURITY.md`) with
   guidance to keep secrets out of VCS.

Step 4 — verification

After forced push and rotation, clone fresh and run the full verify:

```bash
git clone git@github.com:your-org/lets-play.git
cd lets-play
./mvnw -U -DskipTests=false verify -DtrimStackTrace=false
```

Run secret scanning (optional):

```bash
git grep -n "OLD_SECRET_TEXT" || true
# or run gitleaks/truffleHog for a thorough pass
```

Rescue steps for contributors

- Recommended: re-clone the repository fresh.
- Advanced: stash/uncommitted work and rebase/migrate local changes
  onto the rewritten history — see your Git host docs for recovery
  instructions.

Helpers included in this repo

- `scripts/prepare_remove_jwt_secret.sh` — replaces the working-tree
  secret with the placeholder and creates a backup at
  `src/main/resources/application.properties.bak` (do **not** commit
  the backup file). Use it before running the history rewrite.
- `scripts/rewrite_history_remove_jwt.sh` — wraps `git-filter-repo` and
  operates on a temporary mirror clone; the script does NOT push to
  origin. Inspect the mirror and push manually when ready.

Checklist before forcing a push

- [ ] Make a bare backup clone.
- [ ] Inform all collaborators and schedule a maintenance window.
- [ ] Verify you have rights to force-push.
- [ ] Prepare the rotated secret in the target secret manager.

If you want, I can:

- Produce a copy of `replace-jwt-secret.txt` for you to paste the old
  secret into (I will not store or print the secret), or
- Walk you step-by-step through running `scripts/rewrite_history_remove_jwt.sh`
  on your machine when you are ready.

Safety note: I will NOT run any destructive rewrite commands in this
workspace without your explicit confirmation. Follow the mirror ->
inspect -> push pattern above to avoid accidental data loss.
