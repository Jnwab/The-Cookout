## Firebase Emulator Test Account Usage Steps

1.  Using this command via terminal (bash) will auto-load the test account needed for testing:
    ```firebase emulators:start --import=./emulator-data```

The emulator UI will be available at: http://127.0.0.1:4001/

### Test Account Credentials

Use these credentials for testing authentication flows:

- **Email:** test@example.com
- **Password:** password123

### Verifying the Test Account

1. Start the emulator with the import flag (see above)
2. Open the Auth Emulator UI: http://127.0.0.1:4001/auth
3. You should see the test account pre-loaded

### First Time Setup

If this is your first time running the project:

1. Pull the latest from main: `git pull origin main`
2. Install Firebase CLI if needed: `npm install -g firebase-tools`
3. Start the emulator: `firebase emulators:start --import=./emulator-data`

### Creating Additional Test Accounts

If you need to add more test accounts:

1. Start the emulator
2. Create accounts through your app's signup flow or the Auth Emulator UI
3. Export the data: `firebase emulators:export ./emulator-data`
4. Commit and push the updated emulator-data folder

### Troubleshooting

**Emulator starts but test account is missing:**
- Make sure you're using the `--import=./emulator-data` flag
- Verify the `emulator-data/` folder exists in your project root

**"Already logged in" errors:**
- Clear your browser's local storage
- Or use incognito/private browsing mode