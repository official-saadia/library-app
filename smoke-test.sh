#!/usr/bin/env bash
# Post-deployment smoke test for the Library app, using curl.
# Usage: ./smoke-test.sh [base-url]
# Example: ./smoke-test.sh http://client-server:8080
# Exits with a non-zero code if any check fails, so a pipeline stage using it fails too.

BASE_URL="${1:-http://localhost:8080}"
FAILED=0

# Wait up to 60 seconds for the app to start answering.
echo "Waiting for ${BASE_URL}/api/books ..."
for i in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/books" || true)
  if [ "$code" = "200" ]; then
    break
  fi
  sleep 2
done

check_status() {
  local name="$1" expected="$2"
  shift 2
  local actual
  actual=$(curl -s -o /dev/null -w "%{http_code}" "$@")
  if [ "$actual" = "$expected" ]; then
    echo "PASS  $name (HTTP $actual)"
  else
    echo "FAIL  $name (expected HTTP $expected, got $actual)"
    FAILED=1
  fi
}

check_body_contains() {
  local name="$1" expected_text="$2" url="$3"
  local body
  body=$(curl -s "$url")
  if echo "$body" | grep -q "$expected_text"; then
    echo "PASS  $name"
  else
    echo "FAIL  $name (response did not contain '$expected_text')"
    FAILED=1
  fi
}

check_status        "List books"            200 "${BASE_URL}/api/books"
check_status        "Get book 1"            200 "${BASE_URL}/api/books/1"
check_status        "Missing book is 404"   404 "${BASE_URL}/api/books/99999"
check_body_contains "Search finds Dune"     "Dune" "${BASE_URL}/api/books/search?q=dune"
check_status        "Add book"              201 -X POST "${BASE_URL}/api/books" \
  -H "Content-Type: application/json" \
  -d '{"name":"Smoke Test Book","author":"CI","genre":"Testing","description":"Added by smoke-test.sh"}'

if [ "$FAILED" -ne 0 ]; then
  echo "Smoke test FAILED"
  exit 1
fi
echo "Smoke test passed"
