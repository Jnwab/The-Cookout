#!/usr/bin/env bash

set -e

# Where this script lives
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 1) Create virtualenv if it doesn't exist
if [ ! -d "venv" ]; then
  echo "[run_transcription_setup] Creating Python 3 venv in ./venv"
  python3 -m venv venv
fi

# 2) Activate virtualenv
echo "[run_transcription_setup] Activating venv"
# shellcheck disable=SC1091
source venv/bin/activate

# 3) Install dependencies
echo "[run_transcription_setup] Installing dependencies from requirements.txt"
pip install --upgrade pip
pip install -r requirements.txt

# 4) Run the server
echo "[run_transcription_setup] Starting uvicorn on http://127.0.0.1:8000"
uvicorn main:app --reload --port 8000
