import { Router } from "express";
import { OAuth2Client } from "google-auth-library";
import admin from "firebase-admin";

const router = Router();
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// POST /auth/google/verify  { idToken }
router.post("/verify", async (req, res) => {
  try {
    const { idToken } = req.body;
    if (!idToken) return res.status(400).json({ error: "Missing idToken" });

    const ticket = await client.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });
    const payload = ticket.getPayload();
    if (!payload) return res.status(401).json({ error: "Invalid token" });

    const uid = `google:${payload.sub}`;

    try {
      await admin.auth().getUser(uid);
    } catch {
      await admin.auth().createUser({
        uid,
        email: payload.email,
        emailVerified: !!payload.email_verified,
        displayName: payload.name,
        photoURL: payload.picture,
      }).catch((e) => {
        if (e.errorInfo?.code !== "auth/uid-already-exists") throw e;
      });
    }

    const customToken = await admin.auth().createCustomToken(uid, { provider: "google" });
    res.json({ customToken });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "Verification failed" });
  }
});

export default router;
