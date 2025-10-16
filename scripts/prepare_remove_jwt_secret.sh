#!/usr/bin/env bash
# Helper: prepare the working tree by replacing committed JWT secret value
# with a placeholder and committing the change. This reduces the chance of
# reintroducing the old secret after history rewrite.

set -euo pipefail
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

APP_PROPS=src/main/resources/application.properties
if [[ ! -f "$APP_PROPS" ]]; then
  echo "No application.properties found at $APP_PROPS" >&2
  exit 1
fi

# Make a backup copy
cp "$APP_PROPS" "$APP_PROPS".bak

# Replace app.jwt.secret line with a placeholder
if grep -q '^app.jwt.secret=' "$APP_PROPS"; then
  sed -E -i "s~^app\.jwt\.secret=.*~app.jwt.secret=CHANGE_ME_DO_NOT_COMMIT~" "$APP_PROPS"
  echo "Replaced app.jwt.secret in $APP_PROPS"
else
  echo "No app.jwt.secret key found in $APP_PROPS" >&2
  exit 1
fi

# Show the diff for review
git --no-pager diff -- "$APP_PROPS" || true

echo
echo "If the diff looks correct, commit it with:"
echo "  git add $APP_PROPS && git commit -m 'chore(secrets): replace committed JWT secret with placeholder'"

echo "A backup of the original file was saved to $APP_PROPS.bak"
