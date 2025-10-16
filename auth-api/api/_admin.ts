import { initializeApp, cert, getApps } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";

function getServiceAccount() {
  const b64 = process.env.FIREBASE_SERVICE_ACCOUNT_B64!;
  const json = Buffer.from(b64, "base64").toString("utf8");
  return JSON.parse(json);
}

export function adminAuth() {
  if (!getApps().length) initializeApp({ credential: cert(getServiceAccount()) });
  return getAuth();
}
