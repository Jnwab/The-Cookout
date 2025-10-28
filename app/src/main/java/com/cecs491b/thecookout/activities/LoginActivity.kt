package com.cecs491b.thecookout.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.models.User
import com.cecs491b.thecookout.uiScreens.LoginScreen
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleClient: GoogleSignInClient

    private val AUTH_BASE_URL = "http://10.0.2.2:3000"

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    Toast.makeText(this, "No Google ID token.", Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                // Send the ID token to your backend to verify → get a Firebase custom token
                verifyGoogleOnServer(idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("AUTH", "Google sign-in failed", e)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
            Firebase.auth.useEmulator("10.0.2.2", 9100)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        auth = Firebase.auth
        db = Firebase.firestore

        handleDeepLink(intent)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id)) // REQUIRED
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onLoginClick = { email, password -> handleLogin(email, password) },
                        onForgotPasswordClick = {
                            startActivity(Intent(this, ForgotPasswordActivity::class.java))
                        },
                        onGoogleSignInClick = { googleSignInLauncher.launch(googleClient.signInIntent) },
                        onSignupClick = { startActivity(Intent(this, SignupActivity::class.java)) },
                        onPhoneAuthClick = { startActivity(Intent(this, PhoneAuthActivity::class.java)) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        val isCookoutCallback = data.scheme == "cookout" && data.host == "auth" && data.path == "/callback"
        if (!isCookoutCallback) return

        val token = data.getQueryParameter("token")
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Missing token from TikTok callback.", Toast.LENGTH_LONG).show()
            return
        }

        Firebase.auth.signInWithCustomToken(token)
            .addOnSuccessListener {
                Toast.makeText(this, "Signed in with TikTok!", Toast.LENGTH_SHORT).show()
                auth.currentUser?.let { getUserProfile(it.uid) }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "TikTok login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { getUserProfile(it.uid) } ?: run {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verifyGoogleOnServer(idToken: String) {
        val client = OkHttpClient()
        val json = """{"idToken":"$idToken"}"""
        val body = json.toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$AUTH_BASE_URL/googleVerify")
            .post(body)
            .build()

        client.newCall(req).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Server error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val msg = it.body?.string() ?: it.message
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Verify failed: $msg", Toast.LENGTH_LONG).show()
                        }
                        return
                    }
                    val customToken = JSONObject(it.body!!.string()).getString("customToken")
                    Firebase.auth.signInWithCustomToken(customToken)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                auth.currentUser?.let { user ->
                                    // create/load profile then navigate
                                    getUserProfile(user.uid)
                                } ?: runOnUiThread {
                                    Toast.makeText(this@LoginActivity, "No Firebase user.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        task.exception?.message ?: "Custom-token sign-in failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                }
            }
        })
    }
    
    private fun getUserProfile(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    updateUserLastLogin(uid)
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    navigateToProfile()
                } else {
                    auth.currentUser?.let { createUserProfile(it) } ?: run {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createUserProfile(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "User",
            phoneNumber = "",
            photoUrl = "",
            provider = firebaseUser.providerData.firstOrNull()?.providerId ?: "email",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.collection("users").document(firebaseUser.uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Welcome! Profile created.", Toast.LENGTH_SHORT).show()
                navigateToProfile()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserLastLogin(uid: String) {
        db.collection("users").document(uid).update("updatedAt", System.currentTimeMillis())
    }

    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }
}
