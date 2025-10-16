import express from "express";
import { createRequire } from "module";
import { initializeApp, cert } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import crypto from "crypto";
import dotenv from "dotenv";

dotenv.config();

const require = createRequire(import.meta.url);
const serviceAccount = require("./serviceAccountKey.json"); // keep this file in /server

initializeApp({
  credential: cert(serviceAccount),
});

// ----- your existing constants/envs -----
const app = express();
app.use((req, res, next) => {
  res.setHeader("ngrok-skip-browser-warning", "true");
  next();
});
const PORT = process.env.PORT || 3000;
const APP_LINK = process.env.APP_LINK || "cookout://auth/callback";
const PUBLIC_BASE_URL = process.env.PUBLIC_BASE_URL || "http://localhost:3000";
const TTK_KEY = process.env.TIKTOK_CLIENT_KEY;
const TTK_SECRET = process.env.TIKTOK_CLIENT_SECRET;

const REDIRECT_URI = `${PUBLIC_BASE_URL}/tiktokCallback`;

// ----- routes (unchanged other than getAuth() use) -----
app.get("/tiktokStart", (_req, res) => {
  const state = crypto.randomBytes(16).toString("hex");
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
    if (!code) throw new Error("Missing code");

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
    if (!tokenResp.ok || !tokenJson?.access_token) throw new Error("Token exchange failed");
    const accessToken = tokenJson.access_token;

    const profileResp = await fetch("https://open.tiktokapis.com/v2/user/info/", {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    const profileJson = await profileResp.json();
    const openId = profileJson?.data?.user?.open_id;
    const displayName = profileJson?.data?.user?.display_name ?? "TikTok User";
    if (!openId) throw new Error("No open_id from TikTok");

    const uid = `tiktok:${openId}`;
    // ensure user exists (modular: use getAuth())
    try { await getAuth().getUser(uid); }
    catch { await getAuth().createUser({ uid, displayName }); }

    const customToken = await getAuth().createCustomToken(uid, { provider: "tiktok" });

    res.redirect(`${APP_LINK}?token=${encodeURIComponent(customToken)}`);
  } catch (e) {
    res.status(400).send(e?.message ?? "Auth error");
  }
});

app.listen(PORT, () => {
  console.log(`Local TikTok auth server on http://localhost:${PORT}`);
  console.log(`Redirect URI is ${REDIRECT_URI}`);
});
