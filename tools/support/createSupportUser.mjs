import admin from "firebase-admin";
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";

const serviceAccountPath = path.resolve("tools/support/serviceAccountKey.json");
if (!fs.existsSync(serviceAccountPath)) {
  console.error("Missing tools/support/serviceAccountKey.json");
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(
    JSON.parse(fs.readFileSync(serviceAccountPath, "utf8"))
  ),
});

const SUPPORT_EMAIL = "support.thecookout@gmail.com";
const TEMP_PASSWORD = crypto.randomUUID() + "A1!";

async function main() {
  const existing = await admin
    .auth()
    .getUserByEmail(SUPPORT_EMAIL)
    .catch(() => null);

  if (existing) {
    await admin.auth().updateUser(existing.uid, { disabled: true });
    await admin.auth().setCustomUserClaims(existing.uid, { role: "support" });
    console.log(`Updated & disabled existing support user: ${existing.uid}`);
    return;
  }

  const user = await admin.auth().createUser({
    email: SUPPORT_EMAIL,
    emailVerified: true,
    password: TEMP_PASSWORD,
    disabled: true,
    displayName: "TheCookout Support",
  });

  await admin.auth().setCustomUserClaims(user.uid, { role: "support" });
  console.log(`Created disabled support user: ${user.uid}`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
