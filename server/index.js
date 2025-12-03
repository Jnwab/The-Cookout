// server/index.js
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";
import { config as loadEnv } from "dotenv";

// ---------- Load .env (server/.env → ../.env → CWD/.env) ----------
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const envCandidates = [
  path.resolve(__dirname, ".env"),       // <-- server/.env (your case)
  path.resolve(__dirname, "..", ".env"), // repo root
  path.resolve(process.cwd(), ".env"),   // where node was started
];

let loadedEnvPath = null;
for (const p of envCandidates) {
  if (fs.existsSync(p)) {
    const result = loadEnv({ path: p, debug: true });
    if (!result.error) {
      loadedEnvPath = p;
      console.log(`[BOOT] Loaded .env from: ${p}`);
      console.log(
        `[BOOT] Keys: ${Object.keys(result.parsed || {})
          .map((k) => (k.includes("SECRET") || k.includes("KEY") ? `${k}=***` : k))
          .join(", ")}`
      );
      break;
    } else {
      console.warn(`[BOOT] Failed loading .env at ${p}:`, result.error?.message);
    }
  }
}
if (!loadedEnvPath) console.warn("[BOOT] No .env file found; using process.env only.");

import express from "express";
import cors from "cors";
import { createRequire } from "module";
import { initializeApp, cert } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import crypto from "crypto";
import { OAuth2Client } from "google-auth-library";

const require = createRequire(import.meta.url);

// ---------- Env + config ----------
const PORT = process.env.PORT || 3000;
const APP_LINK = process.env.APP_LINK || "cookout://auth/callback";
const PUBLIC_BASE_URL = (process.env.PUBLIC_BASE_URL || `http://localhost:${PORT}`).replace(/\/$/, "");

const TTK_KEY = process.env.TIKTOK_CLIENT_KEY || "";
const TTK_SECRET = process.env.TIKTOK_CLIENT_SECRET || "";
const REDIRECT_URI = `${PUBLIC_BASE_URL}/tiktokCallback`;

const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID || "";

if (!GOOGLE_CLIENT_ID) {
  console.warn("[WARN] GOOGLE_CLIENT_ID is missing. Set it to R.string.default_web_client_id.");
}
if (!TTK_KEY || !TTK_SECRET) {
  console.warn("[WARN] TikTok client key/secret not set; /tiktok* will fail until you add them.");
}

// ---------- Firebase Auth emulator (optional) ----------
if (process.env.USE_AUTH_EMULATOR === "1") {
  process.env.FIREBASE_AUTH_EMULATOR_HOST = "localhost:9100";
  console.log("Using Firebase Auth Emulator at localhost:9100");
}

// ---------- Firebase Admin init ----------
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

// ---------- Express app ----------
const app = express();

const allowList = (process.env.ALLOWED_ORIGINS || "")
  .split(",")
  .map((s) => s.trim())
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
app.use((_, res, next) => {
  res.setHeader("ngrok-skip-browser-warning", "true");
  next();
});

// ---------- PKCE helpers ----------
function b64url(buf) {
  return Buffer.from(buf)
    .toString("base64")
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/g, "");
}
function makeCodeVerifier() {
  return b64url(crypto.randomBytes(32)); // 32–64 bytes is fine
}
function makeCodeChallenge(verifier) {
  const sha = crypto.createHash("sha256").update(verifier).digest();
  return b64url(sha);
}

// Store verifier per OAuth state
const pendingStates = new Map();
const googleClient = new OAuth2Client(GOOGLE_CLIENT_ID);

// ---------- Root + health ----------
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

// ---------- TikTok (with PKCE) ----------
app.get("/tiktokStart", (_req, res) => {
  const state = crypto.randomBytes(16).toString("hex");

  // PKCE
  const verifier = makeCodeVerifier();
  const challenge = makeCodeChallenge(verifier);

  // remember verifier for this state
  pendingStates.set(state, verifier);

  const u = new URL("https://www.tiktok.com/v2/auth/authorize/");
  u.searchParams.set("client_key", TTK_KEY);
  u.searchParams.set("response_type", "code");
  u.searchParams.set("scope", "user.info.basic");
  u.searchParams.set("redirect_uri", REDIRECT_URI);
  u.searchParams.set("state", state);
  u.searchParams.set("code_challenge", challenge);
  u.searchParams.set("code_challenge_method", "S256");

  res.redirect(u.toString());
});

app.get("/tiktokCallback", async (req, res) => {
  try {
    const code = req.query.code?.toString();
    const state = req.query.state?.toString();
    const verifier = state ? pendingStates.get(state) : undefined;

    if (!code || !state || !verifier) throw new Error("Bad or missing state/code");
    pendingStates.delete(state);

    const body = new URLSearchParams({
      client_key: TTK_KEY,
      client_secret: TTK_SECRET,
      code,
      grant_type: "authorization_code",
      redirect_uri: REDIRECT_URI,
      code_verifier: verifier,
    }).toString();

    const tokenResp = await fetch("https://open.tiktokapis.com/v2/oauth/token/", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
    });

    const rawTxt = await tokenResp.text();
    let tokenJson; try { tokenJson = JSON.parse(rawTxt); } catch { tokenJson = {}; }
    if (!tokenResp.ok) { console.error("Token exchange failed:", rawTxt); throw new Error("Token exchange failed"); }

    const accessToken = tokenJson?.data?.access_token || tokenJson?.access_token;
    const openIdFromToken = tokenJson?.data?.open_id || tokenJson?.open_id;
    if (!accessToken) throw new Error("No access_token returned");

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
    try { await getAuth().getUser(uid); }
    catch {
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

// ---------- Google ----------
app.post("/googleVerify", (req, _res, next) => {
  console.log("googleVerify hit", new Date().toISOString());
  next();
});

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

// ---------- Start server (bind all interfaces) ----------
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Auth server on http://localhost:${PORT}`);
  console.log(`TikTok Redirect URI: ${REDIRECT_URI}`);
});
