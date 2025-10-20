#!/usr/bin/env bash
# tools/audit_manual.sh
# Small helper to perform a manual audit sequence: signup -> signin -> extract token -> list/create product
# Usage: bash tools/audit_manual.sh

set -euo pipefail
ROOT_DIR=$(dirname "$(dirname "$0")")
API_URL=${API_URL:-http://localhost:8080}
OUTPUT=${OUTPUT:-tools/audit_manual_output.txt}

# helper: do curl and print status
function do_curl() {
  echo "\n$1" | tee -a "$OUTPUT"
  shift
  curl -i "$@" 2>&1 | tee -a "$OUTPUT"
}

# start fresh output
echo "Audit manual run at: $(date --iso-8601=seconds)" > "$OUTPUT"

# 1) Signup
SIGNUP_PAYLOAD='{"name":"Audit User","username":"audit_user","email":"audit_user@example.com","password":"AuditPass123"}'
SIGNUP_CMD=( -X POST "$API_URL/api/auth/signup" -H "Content-Type: application/json" -d "$SIGNUP_PAYLOAD" )
do_curl "SIGNUP" "${SIGNUP_CMD[@]}"

# 2) Signin
SIGNIN_PAYLOAD='{"email":"audit_user@example.com","password":"AuditPass123"}'
SIGNIN_CMD=( -X POST "$API_URL/api/auth/signin" -H "Content-Type: application/json" -d "$SIGNIN_PAYLOAD" )
# capture raw signin response
echo "\nSIGNIN" | tee -a "$OUTPUT"
RAW_SIGNIN=$(curl -s "${SIGNIN_CMD[@]}")
echo "$RAW_SIGNIN" | tee -a "$OUTPUT"

# 3) extract token (try common fields)
TOKEN=""
if command -v jq >/dev/null 2>&1; then
  TOKEN=$(echo "$RAW_SIGNIN" | jq -r '.accessToken // .token // .jwt // .data.token // .data.accessToken // empty')
else
  TOKEN=$(echo "$RAW_SIGNIN" | sed -n 's/.*"accessToken"\s*:\s*"\([^"]*\)".*/\1/p')
  if [ -z "$TOKEN" ]; then
    TOKEN=$(echo "$RAW_SIGNIN" | sed -n 's/.*"token"\s*:\s*"\([^"]*\)".*/\1/p')
  fi
fi

if [ -z "$TOKEN" ]; then
  echo "\nERROR: Could not extract token from signin response" | tee -a "$OUTPUT"
  exit 1
fi

echo "\nTOKEN=$TOKEN" | tee -a "$OUTPUT"

# 4) List products (public or protected)
echo "\nLIST PRODUCTS" | tee -a "$OUTPUT"
curl -i -H "Authorization: Bearer $TOKEN" "$API_URL/api/products" 2>&1 | tee -a "$OUTPUT"

# 5) Create a product
CREATE_PAYLOAD='{"name":"Audit Product","description":"Created by audit script","price":9.99}'
echo "\nCREATE PRODUCT" | tee -a "$OUTPUT"
curl -i -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d "$CREATE_PAYLOAD" "$API_URL/api/products" 2>&1 | tee -a "$OUTPUT"

# 6) Cleanup: attempt to delete the created product if returned an id (best-effort)
# try to parse the last line for an _id or id
LAST_ID=$(tail -n 200 "$OUTPUT" | sed -n 's/.*"_id"\s*:\s*"\([^"]*\)".*/\1/p' | tail -n1)
if [ -z "$LAST_ID" ]; then
  LAST_ID=$(tail -n 200 "$OUTPUT" | sed -n 's/.*"id"\s*:\s*"\([^"]*\)".*/\1/p' | tail -n1)
fi
if [ -n "$LAST_ID" ]; then
  echo "\nDELETE PRODUCT $LAST_ID" | tee -a "$OUTPUT"
  curl -i -X DELETE -H "Authorization: Bearer $TOKEN" "$API_URL/api/products/$LAST_ID" 2>&1 | tee -a "$OUTPUT"
else
  echo "\nNo product ID found to delete (skipping cleanup)" | tee -a "$OUTPUT"
fi

echo "\nAudit manual complete, output saved to $OUTPUT" | tee -a "$OUTPUT"
exit 0
