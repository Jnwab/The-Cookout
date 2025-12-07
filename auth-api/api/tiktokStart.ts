export const config = { runtime: "nodejs18.x" };

export default async function handler(req, res) {
  const CLIENT_KEY = process.env.TIKTOK_CLIENT_KEY!;
  const PROJECT_ID = process.env.FIREBASE_PROJECT_ID!;
  const REGION = process.env.REGION || "us-central1";

  // You can replace with your custom Vercel domain later if you add one
  const redirectUri = `https://${PROJECT_ID}.vercel.app/api/tiktokCallback`;

  const state = [...crypto.getRandomValues(new Uint8Array(16))]
    .map(b => b.toString(16).padStart(2,"0")).join("");

  const u = new URL("https://www.tiktok.com/v2/auth/authorize/");
  u.searchParams.set("client_key", CLIENT_KEY);
  u.searchParams.set("response_type", "code");
  u.searchParams.set("scope", "user.info.basic");
  u.searchParams.set("redirect_uri", redirectUri);
  u.searchParams.set("state", state);

  res.status(302).setHeader("Location", u.toString()).end();
}
