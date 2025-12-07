from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from speechmatics.batch import AsyncClient  # Speechmatics Batch client
import google.generativeai as genai
from dotenv import load_dotenv
import httpx

import os
import tempfile
import re
import json

# Load environment variables from .env
load_dotenv()

app = FastAPI()

# --- Env vars / config ---
SPEECHMATICS_API_KEY = os.getenv("SPEECHMATICS_API_KEY")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
SCRAPECREATORS_API_KEY = os.getenv("SCRAPECREATORS_API_KEY")
SCRAPECREATORS_BASE_URL = os.getenv("SCRAPECREATORS_BASE_URL")

if not SPEECHMATICS_API_KEY:
    raise RuntimeError("SPEECHMATICS_API_KEY is not set. Define it in your .env file.")
if not GEMINI_API_KEY:
    raise RuntimeError("GEMINI_API_KEY is not set. Define it in your .env file.")

genai.configure(api_key=GEMINI_API_KEY)
GEMINI_MODEL_NAME = "gemini-2.5-flash-lite"


class TikTokUrlRequest(BaseModel):
    url: str


@app.get("/")
async def root():
    return {"status": "ok", "message": "Recipe parser backend (file upload + TikTok URL)"}

# helper function - parse with gemini
def parse_transcript_to_recipe(transcript: str) -> dict:
    """
    Given a transcript string, call Gemini and return a dict
    shaped exactly like the RecipeDto on Android.
    """
    prompt = f"""
You are a tool that extracts cooking recipes from transcripts of short cooking videos.

Given the transcript below, extract a single recipe and output ONLY valid JSON (no backticks, no markdown, no explanations).

The JSON MUST have this exact shape (camelCase keys):

{{
  "name": string,
  "description": string,
  "prepTimeMinutes": integer or null,
  "cookTimeMinutes": integer or null,
  "servings": integer or null,
  "difficulty": "easy" | "medium" | "hard" | null,
  "ingredients": [
    {{
      "name": string,
      "quantity": string or null,
      "unit": string or null,
      "notes": string or null
    }}
  ],
  "steps": [ string ]
}}

Rules:
- If a value is not explicitly mentioned, make a reasonable guess OR set it to null.
- difficulty should be inferred from the overall complexity of the recipe if possible.
- ingredients should be a clean list with one ingredient per entry.
- steps should be a clear, ordered list of instructions.

Transcript:
\"\"\"{transcript}\"\"\"
"""

    try:
        model = genai.GenerativeModel(GEMINI_MODEL_NAME)
        gemini_response = model.generate_content(prompt)
        raw_text = gemini_response.text or ""
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini API error: {e}")

    cleaned = raw_text.strip()

    # Strip ```json ... ``` if Gemini wraps it
    if cleaned.startswith("```"):
        cleaned = cleaned.strip("`")
        if cleaned.lower().startswith("json"):
            cleaned = cleaned[4:].strip()

    try:
        recipe_obj = json.loads(cleaned)
    except json.JSONDecodeError as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to parse JSON from Gemini response: {e}. Raw output was: {raw_text}"
        )

    expected_keys = {
        "name",
        "description",
        "prepTimeMinutes",
        "cookTimeMinutes",
        "servings",
        "difficulty",
        "ingredients",
        "steps",
    }
    missing = expected_keys - recipe_obj.keys()
    if missing:
        raise HTTPException(
            status_code=500,
            detail=f"Recipe JSON missing keys: {missing}. Raw output: {raw_text}"
        )

    return recipe_obj


# file upload endpoint
@app.post("/parse_recipe")
async def parse_recipe(file: UploadFile = File(...)):
    # match file type
    if not file.filename.lower().endswith((
        ".mp4", ".m4a", ".mp3", ".wav", ".webm", ".mpeg", ".ogg"
    )):
        raise HTTPException(status_code=400, detail="File must be a video or audio file")

    try:
        with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(file.filename)[1]) as tmp:
            contents = await file.read()
            tmp.write(contents)
            tmp_path = tmp.name
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to save uploaded file: {e}")

    try:
        # Transcribe with Speechmatics
        try:
            async with AsyncClient() as sm_client:
                result = await sm_client.transcribe(tmp_path)
                transcript = result.transcript_text
        except Exception as e:
            raise HTTPException(status_code=502, detail=f"Speechmatics transcription error: {e}")

        if not transcript or len(transcript.strip()) == 0:
            raise HTTPException(status_code=400, detail="Transcription result was empty.")

        recipe_obj = parse_transcript_to_recipe(transcript)
        return JSONResponse(content=recipe_obj)

    finally:
        try:
            os.remove(tmp_path)
        except Exception:
            pass


# tiktok link transcription endpoint
@app.post("/parse_recipe_tiktok")
async def parse_recipe_tiktok(body: TikTokUrlRequest):
    if not SCRAPECREATORS_API_KEY or not SCRAPECREATORS_BASE_URL:
        raise HTTPException(
            status_code=500,
            detail="SCRAPECREATORS_API_KEY or SCRAPECREATORS_BASE_URL not configured in .env",
        )

    tiktok_url = body.url.strip()
    if not tiktok_url:
        raise HTTPException(status_code=400, detail="TikTok URL cannot be empty.")

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.get(
                #url=f"{SCRAPECREATORS_BASE_URL}/v1/tiktok/video/transcript?url={tiktok_url}",
                SCRAPECREATORS_BASE_URL,
                headers={
                    "x-api-key": SCRAPECREATORS_API_KEY,
                    "Accept": "application/json",
                },
                params={
                    "url": tiktok_url,
                    "language": "en",
                    #"region": "US",
                    #"trim": "true",
                },
            )
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Error calling ScrapeCreators: {e}")

    if response.status_code != 200:
        raise HTTPException(
            status_code=502,
            detail=f"ScrapeCreators returned {response.status_code}: {response.text}",
        )

    data = response.json()

    transcript_raw = data.get("transcript", "")
    if not transcript_raw or not transcript_raw.strip():
        print("\n========== SCRAPECREATORS RAW RESPONSE (DEBUG) ==========")
        try:
            print(json.dumps(data, indent=2))
        except Exception:
            print("Non-JSON response:", data)
        print("=========================================================\n")

        raise HTTPException(
            status_code=400,
            detail="ScrapeCreators transcript was empty or missing.",
        )

    # Clean WEBVTT → plain text
    transcript = clean_transcript(transcript_raw)

    recipe_obj = parse_transcript_to_recipe(transcript)
    return JSONResponse(content=recipe_obj)


def clean_transcript(text: str) -> str:
    """
    If the transcript is in WEBVTT format, strip the WEBVTT header and
    timestamp lines, returning just the spoken text.
    Otherwise return the original text.
    """
    stripped = text.lstrip()
    if not stripped.upper().startswith("WEBVTT"):
        return text

    lines = []
    for line in stripped.splitlines():
        s = line.strip()
        if not s:
            continue
        if s.upper() == "WEBVTT":
            continue
        # skip timestamp lines like "00:00:00.140 --> 00:00:04.020"
        if re.match(r"\d{2}:\d{2}:\d{2}\.\d{3}\s+-->\s+\d{2}:\d{2}:\d{2}\.\d{3}", s):
            continue
        lines.append(s)
    return " ".join(lines)
