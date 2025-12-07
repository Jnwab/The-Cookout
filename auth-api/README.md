# Cookout Auth API (TikTok → Firebase Custom Token)

**Endpoints**
- `GET /api/tiktokStart` → Redirect to TikTok consent
- `GET /api/tiktokCallback` → Exchange code, mint Firebase Custom Token, redirect to `cookout://auth/callback?token=...`

**Environment Variables**
- `TIKTOK_CLIENT_KEY`
- `TIKTOK_CLIENT_SECRET`
- `FIREBASE_PROJECT_ID`
- `REGION` (e.g., `us-central1`)
- `APP_LINK` (e.g., `cookout://auth/callback`)
- `FIREBASE_SERVICE_ACCOUNT_B64` (base64 of Firebase service account JSON)
