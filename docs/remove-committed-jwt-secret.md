# Remove committed JWT secret from repository history

This document provides a safe, repeatable plan to remove an accidentally committed JWT secret from Git history, rotate the secret in production, and update team/dev workflows.

High-level summary

- The working tree already contains a placeholder value in `src/main/resources/application.properties` (`app.jwt.secret=CHANGE_ME_DO_NOT_COMMIT`).
- However, the secret exists in the Git history and must be removed using a history-rewrite tool (git-filter-repo or BFG).
- Rewriting history is destructive: it changes commit SHAs. Coordinate with your team and follow the steps below.

Prerequisites and important cautions

- Back up the repository (a bare clone). Do not run any of the history-rewrite steps on the main/shared clone without a backup.
- All collaborators must re-clone after a forced push, or follow the recovery steps below.
- You need sufficient rights to force-push to the remote branch(es) you rewrite.
- Prefer `git-filter-repo` (recommended) over BFG when available; `git-filter-repo` is faster and more flexible.
- If your project is hosted on a platform (GitHub/GitLab/Bitbucket) with protected branches, create a temporary branch and coordinate unprotecting/pushing as appropriate.

Step 0 — Prepare and validate (non-destructive)

1. Identify all occurrences of likely secrets (quick scan):

```bash
# from the repository root
# show lines that contain the literal secret if you know it
git grep -n "YOUR_OLD_JWT_SECRET" || true

# or scan for likely secret patterns (simple heuristics)
git grep -n "app.jwt.secret" || true
```

2. Create a backup bare clone (do this before any destructive operations):

```bash
# adjust path as needed
cd $(git rev-parse --show-toplevel)
mkdir -p ../lets-play-backups
git clone --mirror . ../lets-play-backups/lets-play-mirror.git
```

3. Replace secrets in the working tree with placeholders and commit that change (this avoids reintroducing the secret after history rewrite):

```bash
# run the helper script provided in scripts/prepare_remove_jwt_secret.sh
bash scripts/prepare_remove_jwt_secret.sh
# inspect changes and commit
git status
git add -A
git commit -m "chore(secrets): replace committed JWT secret with placeholder in working tree"
```

Step 1 — Choose a history-rewrite tool
Option A (recommended): git-filter-repo (requires Python and installation)

- Install: `pip install git-filter-repo` or use your package manager if available.
- The following command removes the secret string from all files in every commit and replaces it with the placeholder.

Option B (alternative): BFG Repo-Cleaner (Java jar). BFG is simpler for removing whole files or replacing text but `git-filter-repo` is preferred.

Step 2 — Use git-filter-repo to remove/replace the secret

- Example: replace literal secret value with placeholder across history.

```bash
# from a fresh clone (not the working repo that others are using)
# 1) Clone a fresh mirror
git clone --mirror https://example.com/your/repo.git repo-mirror.git
cd repo-mirror.git

# 2) Run git-filter-repo replacement
# Replace OLD_SECRET_TEXT (exact literal) with CHANGE_ME_DO_NOT_COMMIT
# NOTE: This modifies all refs in the mirror
git filter-repo --replace-text <(printf "OLD_SECRET_TEXT==>CHANGE_ME_DO_NOT_COMMIT\n")

# 3) Inspect the rewritten history (use git log, git grep)
git grep -n "OLD_SECRET_TEXT" || true

# 4) Push the rewritten refs back to the origin (force)
# Be sure you understand the control-plane implications before pushing
git push --force --mirror origin
```

If the secret was stored in multiple forms (URL-encoded or PEM-wrapped), add each form as a replacement line.

Step 2 (alternate) — Use BFG to remove secrets

- BFG can remove a file or replace strings. Example:

```bash
# Create a mirror
git clone --mirror https://example.com/your/repo.git repo-mirror.git
cd repo-mirror.git

# Remove any file named containing "secrets.properties" (example)
java -jar /path/to/bfg.jar --delete-files secrets.properties

# Or replace text across history using --replace-text file
# Create replace.txt containing a line with the exact secret to replace:
#  OLD_SECRET_TEXT==>CHANGE_ME_DO_NOT_COMMIT
java -jar /path/to/bfg.jar --replace-text replace.txt

# After BFG run, run the following to clean up:
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Push the rewritten refs
git push --force --mirror origin
```

Step 3 — Post-rewrite cleanup and team coordination

1. Notify team: every collaborator must re-clone the repository (or follow the rescue steps below).
2. Rotate the secret used by all deployed environments (CI/CD, secret managers, keys):
   - Create a new secret value in your secret manager (Vault, AWS Secrets Manager, GitHub/GitLab CI variable, etc.).
   - Update production/staging deployments to use the new `APP_JWT_SECRET` value.
3. Revoke any tokens that might have been signed with the old secret (if your system supports revocation) — otherwise, shorten token lifetime temporarily.
4. Update the project README / SECURITY.md with the new secret handling guidance.

Step 4 — Verifications

- Locally: after forced push, clone the repository again and run an all-tests build to confirm everything still passes.

```bash
# clone afresh
git clone git@github.com:your-org/lets-play.git
cd lets-play
./mvnw -U -DskipTests=false verify -DtrimStackTrace=false
```

- Search for lingering secrets:

```bash
git grep -n "OLD_SECRET_TEXT" || true
# optionally run truffleHog or other secret scanners
```

Rescue steps for contributors (if they have an old clone)

- Easiest: reclone the repository fresh.
- Advanced: rebase local work onto the new rewritten branch; see GitHub docs 'How to recover from a forced push'

Other notes and best practices

- Don't store secrets in VCS. Use environment variables, Vault, or your cloud provider's secrets manager.
- Add local overrides to `.gitignore` if you keep local `application.properties` with secrets (but never commit them).
- Consider adding pre-commit hooks (e.g., git-secrets) to prevent future mistakes.
- Consider adding a CI secret-scanning step (truffleHog, gitleaks) to detect accidental check-ins.

Contact/Coordination checklist before forcing a push

- [ ] Make a bare backup clone.
- [ ] Inform all repo collaborators of the maintenance window.
- [ ] Confirm you have required rights to force-push.
- [ ] Prepare the rotated secret in the target secrets manager.

---

If you want, I can:

- Generate the helper script that replaces the secret in the working tree and commits the placeholder (I added `scripts/prepare_remove_jwt_secret.sh` as a helper below).
- Show the exact `git-filter-repo` or BFG commands customized to the exact secret string (I can inject the concrete secret if you provide it). For safety I won't include any sensitive literal in the docs unless you confirm.
- Walk you through performing the `git-filter-repo` operation interactively.
