#!/usr/bin/env bash
# rewrite_history_remove_jwt.sh
# Safe helper to rewrite Git history and replace a committed JWT secret using git-filter-repo.
# This script operates on a fresh mirror clone under a temp directory and does NOT push to origin.
# You must inspect the rewritten mirror and coordinate a forced push when ready.

set -euo pipefail
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

BAK=src/main/resources/application.properties.bak
if [[ ! -f "$BAK" ]]; then
  echo "Backup file $BAK not found. Ensure you ran scripts/prepare_remove_jwt_secret.sh first." >&2
  exit 1
fi

# Extract the secret value from the backup without printing it.
SECRET_LINE=$(grep '^app.jwt.secret=' "$BAK" || true)
if [[ -z "$SECRET_LINE" ]]; then
  echo "Could not find 'app.jwt.secret=' line in $BAK" >&2
  exit 1
fi
SECRET_VALUE=${SECRET_LINE#app.jwt.secret=}
if [[ -z "$SECRET_VALUE" ]]; then
  echo "Secret value appears empty in $BAK; aborting" >&2
  exit 1
fi

# Check git-filter-repo availability
if ! command -v git-filter-repo >/dev/null 2>&1; then
  echo "git-filter-repo not found. Install with: pip install git-filter-repo" >&2
  exit 1
fi

# Create a fresh mirror clone in a temp dir
TMPDIR=$(mktemp -d)
MIRROR="$TMPDIR/lets-play-mirror.git"
echo "Creating mirror clone in $MIRROR"
git clone --mirror "$REPO_ROOT" "$MIRROR"
cd "$MIRROR"

# Build replace-text file. This file will contain a single replacement mapping:
#   <OLD_SECRET>==>CHANGE_ME_DO_NOT_COMMIT
# We write the file but do NOT echo the secret to stdout.
REPLACE_FILE="$TMPDIR/replace-jwt-secret.txt"
printf "%s==>CHANGE_ME_DO_NOT_COMMIT\n" "$SECRET_VALUE" > "$REPLACE_FILE"
chmod 600 "$REPLACE_FILE"

echo "Prepared replace-text file at: $REPLACE_FILE"

# WARNING: The next operation rewrites all refs in this mirror. It's destructive to history
# inside this mirror only. We do not push changes to origin in this script. Inspect results first.

echo "Running git-filter-repo (this may take a while)..."
# Note: git-filter-repo will rewrite the mirror in-place
git filter-repo --replace-text "$REPLACE_FILE"

# Quick verification: test if secret still exists in rewritten mirror (without printing it)
if git grep -qF -- "$SECRET_VALUE"; then
  echo "WARNING: secret still found in rewritten mirror (investigate)" >&2
else
  echo "Success: no occurrences of the old secret found in the rewritten mirror." 
fi

# Show guidance to push (manual step)
cat <<'EOF'
The mirror rewrite completed. Next steps (manual):
1) Inspect the rewritten mirror in "$MIRROR".
   - Use git log, git grep to inspect.
2) When ready, push rewritten refs to origin with:
   git push --force --mirror origin
   (Run this from inside the mirror clone directory.)
3) Notify collaborators to reclone the repository or follow recovery instructions.

IMPORTANT: Do NOT push until you have backups and prepared the team for forced-push effects.
EOF

# Print mirror location for user
echo "Mirror location: $MIRROR"

# Keep the temp dir around for inspection. Do NOT auto-delete.
echo "NOTE: temp dir left for inspection: $TMPDIR"

exit 0
