@echo off
setlocal enabledelayedexpansion

echo [run_transcription_setup] Changing to script directory
cd /d %~dp0

REM 1) Create virtualenv if it doesn't exist
if not exist venv (
    echo [run_transcription_setup] Creating Python venv in .\venv
    REM If "python" doesn't work, change this to "py -3.11"
    python -m venv venv
)

REM 2) Activate virtualenv
echo [run_transcription_setup] Activating venv
call venv\Scripts\activate

REM 3) Install dependencies
echo [run_transcription_setup] Installing dependencies from requirements.txt
python -m pip install --upgrade pip
python -m pip install -r requirements.txt

REM 4) Run the server
echo [run_transcription_setup] Starting uvicorn on http://127.0.0.1:8000
uvicorn main:app --reload --port 8000

endlocal
