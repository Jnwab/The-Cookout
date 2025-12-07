import { adminAuth } from "./_admin";

export const config = { runtime: "nodejs18.x" };

export default async function handler(req, res) {
  try {
    const code = req.query.code as string | undefined;
    if (!code) throw new Error("Missing code");

    const CLIENT_KEY = process.env.TIKTOK_CLIENT_KEY!;
    const CLIENT_SECRET = process.env.TIKTOK_CLIENT_SECRET!;
    const PROJECT_ID = process.env.FIREBASE_PROJECT_ID!;
    const REGION = process.env.REGION || "us-central1";
    const APP_LINK = process.env.APP_LINK || "cookout://auth/callback";

    const redirectUri = `https://${PROJECT_ID}.vercel.app/api/tiktokCallback`;

    // 1) code -> access token
    const tokenResp = await fetch("https://open.tiktokapis.com/v2/oauth/token/", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        client_key: CLIENT_KEY,
        client_secret: CLIENT_SECRET,
        code,
        grant_type: "authorization_code",
        redirect_uri: redirectUri
      }).toString(),
    });
    const tokenJson: any = await tokenResp.json();
    if (!tokenResp.ok || !tokenJson?.access_token) throw new Error("Token exchange failed");

    // 2) user info
    const profileResp = await fetch("https://open.tiktokapis.com/v2/user/info/", {
      headers: { Authorization: `Bearer ${tokenJson.access_token}` },
    });
    const profileJson: any = await profileResp.json();
    const openId = profileJson?.data?.user?.open_id;
    const displayName = profileJson?.data?.user?.display_name ?? "TikTok User";
    if (!openId) throw new Error("No open_id");

    // 3) Firebase custom token
    const uid = `tiktok:${openId}`;
    const auth = adminAuth();
    try { await auth.getUser(uid); } catch { await auth.createUser({ uid, displayName }); }
    const customToken = await auth.createCustomToken(uid, { provider: "tiktok" });

    // 4) back to app
    res.status(302).setHeader("Location", `${APP_LINK}?token=${encodeURIComponent(customToken)}`).end();
  } catch (e) {
    res.status(400).send((e as Error).message);
  }
}
