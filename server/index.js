// server/index.js
import express from "express";
import cors from "cors";
import { createRequire } from "module";
import { initializeApp, cert } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import crypto from "crypto";
import dotenv from "dotenv";
import { OAuth2Client } from "google-auth-library";

dotenv.config();

const require = createRequire(import.meta.url);

// ---- Env + sanity checks ----
const PORT = process.env.PORT || 3000;
const APP_LINK = process.env.APP_LINK || "cookout://auth/callback";
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL || `http://localhost:${PORT}`;

const TTK_KEY = process.env.TIKTOK_CLIENT_KEY || "";
const TTK_SECRET = process.env.TIKTOK_CLIENT_SECRET || "";
const REDIRECT_URI = `${PUBLIC_BASE_URL}/tiktokCallback`;

const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID || "";
if (!GOOGLE_CLIENT_ID) {
  console.warn(
    "[WARN] GOOGLE_CLIENT_ID is missing. Set it to the value of R.string.default_web_client_id."
  );
}
if (!TTK_KEY || !TTK_SECRET) {
  console.warn("[WARN] TikTok client key/secret not set; /tiktok* will fail until you add them.");
}

// ---- Use Firebase Auth emulator in dev (optional) ----
if (process.env.USE_AUTH_EMULATOR === "1") {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = "localhost:9100";
  console.log("Using Firebase Auth Emulator at localhost:9100");
}

// ---- Firebase Admin init (file or ENV fallback) ----
let serviceCreds = null;
try {
  serviceCreds = require("./serviceAccountKey.json");
} catch {
  if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
    serviceCreds = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
  } else {
    throw new Error(
      "Missing Firebase credentials: add server/serviceAccountKey.json or FIREBASE_SERVICE_ACCOUNT_JSON"
    );
  }
}
initializeApp({ credential: cert(serviceCreds) });

// ---- App setup ----
const app = express();

// (Optional) tighten CORS if you want:
const allowList = (process.env.ALLOWED_ORIGINS || "")
  .split(",")
  .map(s => s.trim())
  .filter(Boolean);
app.use(
  cors(
    allowList.length
      ? {
          origin(origin, cb) {
            if (!origin || allowList.includes(origin)) cb(null, true);
            else cb(new Error("Not allowed by CORS"));
          },
        }
      : {}
  )
);

app.use(express.json());
app.use((req, res, next) => {
  res.setHeader("ngrok-skip-browser-warning", "true");
  next();
});

const pendingStates = new Set();
const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

// ---- Helpful root + health ----
app.get("/", (_req, res) => {
  res.send(`
    <h1>The Cookout Auth Server</h1>
    <ul>
      <li>Health: <a href="/health">/health</a></li>
      <li>TikTok start: <a href="/tiktokStart">/tiktokStart</a></li>
      <li>Google verify: POST /googleVerify</li>
      <li>Redirect URI: <code>${REDIRECT_URI}</code></li>
    </ul>
  `);
});
app.get("/health", (_req, res) => res.json({ ok: true }));

// ===================== TikTok =====================
app.get("/tiktokStart", (_req, res) => {
  const state = crypto.randomBytes(16).toString("hex");
  pendingStates.add(state);

  const u = new URL("https://www.tiktok.com/v2/auth/authorize/");
  u.searchParams.set("client_key", TTK_KEY);
  u.searchParams.set("response_type", "code");
  u.searchParams.set("scope", "user.info.basic");
  u.searchParams.set("redirect_uri", REDIRECT_URI);
  u.searchParams.set("state", state);

  res.redirect(u.toString());
});

app.get("/tiktokCallback", async (req, res) => {
  try {
    const code = req.query.code?.toString();
    const state = req.query.state?.toString();
    if (!code || !state || !pendingStates.has(state)) throw new Error("Bad or missing state/code");
    pendingStates.delete(state);

    const tokenResp = await fetch("https://open.tiktokapis.com/v2/oauth/token/", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        client_key: TTK_KEY,
        client_secret: TTK_SECRET,
        code,
        grant_type: "authorization_code",
        redirect_uri: REDIRECT_URI,
      }).toString(),
    });
    const tokenJson = await tokenResp.json();
    const accessToken = tokenJson?.data?.access_token || tokenJson?.access_token;
    const openIdFromToken = tokenJson?.data?.open_id || tokenJson?.open_id;
    if (!tokenResp.ok || !accessToken) throw new Error("Token exchange failed");

    const profileResp = await fetch(
      "https://open.tiktokapis.com/v2/user/info/?fields=open_id,display_name,avatar_url",
      { headers: { Authorization: `Bearer ${accessToken}` } }
    );
    const profileJson = await profileResp.json();
    const user = profileJson?.data?.user;
    if (!user?.open_id || (openIdFromToken && user.open_id !== openIdFromToken)) {
      throw new Error("User mismatch");
    }

    const uid = `tiktok:${user.open_id}`;
    try {
      await getAuth().getUser(uid);
    } catch {
      await getAuth().createUser({
        uid,
        displayName: user.display_name ?? "TikTok User",
        photoURL: user.avatar_url,
      });
    }

    const customToken = await getAuth().createCustomToken(uid, { provider: "tiktok" });
    res.redirect(`${APP_LINK}?token=${encodeURIComponent(customToken)}`);
  } catch (e) {
    console.error(e);
    res.status(400).send(e?.message ?? "Auth error");
  }
});

// ===================== Google =====================
// Debug log so you can see the hit
app.post("/googleVerify", (req, _res, next) => {
  console.log("googleVerify hit", new Date().toISOString());
  next();
});

// Verifies Google ID token (audience = GOOGLE_CLIENT_ID) and returns Firebase custom token
app.post("/googleVerify", async (req, res) => {
  try {
    const { idToken } = req.body || {};
    if (!idToken) return res.status(400).json({ error: "Missing idToken" });
    if (!GOOGLE_CLIENT_ID) return res.status(500).json({ error: "Server misconfigured: GOOGLE_CLIENT_ID" });

    const ticket = await googleClient.verifyIdToken({ idToken, audience: GOOGLE_CLIENT_ID });
    const payload = ticket.getPayload();
    if (!payload) return res.status(401).json({ error: "Invalid token" });

    const uid = `google:${payload.sub}`;
    try {
      await getAuth().getUser(uid);
    } catch {
      await getAuth()
        .createUser({
          uid,
          email: payload.email,
          emailVerified: !!payload.email_verified,
          displayName: payload.name,
          photoURL: payload.picture,
        })
        .catch((err) => {
          if (err?.errorInfo?.code !== "auth/uid-already-exists") throw err;
        });
    }

    const customToken = await getAuth().createCustomToken(uid, { provider: "google" });
    res.json({ customToken });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Verification failed" });
  }
});

// ---- Start server ----
app.listen(PORT, () => {
  console.log(`Auth server on http://localhost:${PORT}`);
  console.log(`TikTok Redirect URI: ${REDIRECT_URI}`);
});
