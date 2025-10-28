import { Router } from "express";
import axios from "axios";
import admin from "firebase-admin";
import crypto from "node:crypto";

const router = Router();

const TIKTOK_CLIENT_KEY = process.env.TIKTOK_CLIENT_KEY;
const TIKTOK_CLIENT_SECRET = process.env.TIKTOK_CLIENT_SECRET;
const REDIRECT_URI = process.env.TIKTOK_REDIRECT_URI;

// simple in-memory state for demo
const states = new Set();

// Your app already calls /tiktokStart — keep it:
router.get("/start", (req, res) => {
  const state = crypto.randomUUID();
  states.add(state);

  const scope = "user.info.basic";
  const url =
    `https://www.tiktok.com/v2/auth/authorize/` +
    `?client_key=${encodeURIComponent(TIKTOK_CLIENT_KEY)}` +
    `&scope=${encodeURIComponent(scope)}` +
    `&response_type=code` +
    `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
    `&state=${encodeURIComponent(state)}`;

  res.redirect(url);
});

// TikTok redirects here with ?code=&state=
router.get("/callback", async (req, res) => {
  try {
    const { code, state } = req.query;
    if (!code || !state || !states.has(state)) return res.status(400).send("Bad state/code");
    states.delete(state);

    // Exchange code → token
    const tokenResp = await axios.post(
      "https://open.tiktokapis.com/v2/oauth/token/",
      {
        client_key: TIKTOK_CLIENT_KEY,
        client_secret: TIKTOK_CLIENT_SECRET,
        code,
        grant_type: "authorization_code",
        redirect_uri: REDIRECT_URI,
      },
      { headers: { "Content-Type": "application/json" } }
    );

    const { access_token, open_id } = tokenResp.data?.data || {};
    if (!access_token || !open_id) return res.status(401).send("Token exchange failed");

    // Prove the token by calling user info
    const me = await axios.get("https://open.tiktokapis.com/v2/user/info/", {
      headers: { Authorization: `Bearer ${access_token}` },
      params: { fields: "open_id,display_name,avatar_url" },
    });

    const user = me.data?.data?.user;
    if (!user || user.open_id !== open_id) return res.status(401).send("User mismatch");

    const uid = `tiktok:${open_id}`;
    try {
      await admin.auth().getUser(uid);
    } catch {
      await admin.auth().createUser({
        uid,
        displayName: user.display_name,
        photoURL: user.avatar_url,
      }).catch((e) => {
        if (e.errorInfo?.code !== "auth/uid-already-exists") throw e;
      });
    }

    const customToken = await admin.auth().createCustomToken(uid, { provider: "tiktok" });

    // For now return JSON; later you can deep-link back to the app with this token
    res.json({ customToken });
  } catch (e) {
    console.error(e?.response?.data || e);
    res.status(500).send("TikTok verification failed");
  }
});

export default router;
