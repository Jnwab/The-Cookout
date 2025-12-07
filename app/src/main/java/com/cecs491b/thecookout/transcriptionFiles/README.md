readme for setting up transcription with speechmatic and gemini

1. upload video or paste tiktok link
2. speechmatics or scrapecreators transcribes video
3. gemini parses text
4. returns a json with our categories to autofill

app calls api requests in emulator via ngrok tunnel (grok ai !?!?)

prereqs
1. python 3.11+
2. pip
3. ngrok (https://ngrok.com)

.env explanation
this file exists so that you dont have to manually set env variables
copy the .env.example file, rename is ".env", and paste in your api keys

main apis
speechmatics (480 minutes of speech-to-text per month for free, can signup w/ google in like 12 seconds)
scrapecreators (100 free tokens of tiktok transcriptions, 1 token per video)
gemini (limited free tokens, unclear how many but should prolly be enough for us)
once accounts have been made for these (just sign in w/ google for both, gemini should alr be done):
create api keys and paste them into .env
WARNING!: speechmatics api key will only display once, be ready to paste it in as soon as you generate it
or store it somewhere safe

other necessary stuff
ngrok
- go to website and follow steps to download and setup until uvicorn line (script takes care of this on a diff port)
fastapi
- backend framework that receives requests, handles api calls, and returns formatted results
- can also use localhost:[PORT]/docs to simulate usage outside of the emulator

getting started:
chmod +x run_transcription_setup.sh
- run this once to make the scrip executable
./run_transcription_setup.sh
- use the bat file if youre a stinky windows user
- this creates the python venv, activates it, installs all packages from requirements.txt, runs fastapi app
- server is available at http://127.0.0.1:8000 after you run the script, should display a status ok message
[in separate terminal]
ngrok http 8000
- it should print Forwarding  https://1234-56-78-90-1.ngrok-free.app -> http://localhost:8000
- copy the first link into retrofitclient.base_url (must add "/" at the end when you paste it)
- locate this file in com.cecs491b.thecookout/network/retrofitclient.kt
- IMPORTANT! this must be redone every time you start a new ngrok tunnel


~~ SIMPLIFIED STEPS ~~ (checklist to make sure it'll work right)
1. run setup script
2. start ngrok
3. update retrofitclient.base_url
4. start other stuff like firebase
5run cookout app / access localhost to simulate transcription feature
6.
7.
8.
9. profit???

p.s. go to localhost:8000/docs to test backend without starting the app
p.p.s. start firebase with this command --> npx firebase-tools emulators:start   (after navigating to where firebase.json exists)