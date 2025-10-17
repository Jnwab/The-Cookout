package com.cecs491b.thecookout.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.uiScreens.LoginScreen
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.cecs491b.thecookout.models.User

class LoginActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleClient: GoogleSignInClient
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.auth.useEmulator("10.0.2.2", 9100)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        auth = Firebase.auth
        database = Firebase.database

        handleDeepLink(intent)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
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
                        onGoogleSignInClick = { launchGoogleSignIn() },
                        onSignupClick = {
                            startActivity(Intent(this, SignupActivity::class.java))
                        },
                        onPhoneAuthClick = {
                            startActivity(Intent(this, PhoneAuthActivity::class.java))
                        }
                    )
                }
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        val isCookoutCallback =
            data.scheme == "cookout" && data.host == "auth" && data.path == "/callback"

        if (!isCookoutCallback) return

        val token = data.getQueryParameter("token")
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Missing token from TikTok callback.", Toast.LENGTH_LONG).show()
            return
        }

        Firebase.auth.signInWithCustomToken(token)
            .addOnSuccessListener {
                Toast.makeText(this, "Signed in with TikTok!", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to your main screen here
                // startActivity(Intent(this, MainActivity::class.java))
                // finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "TikTok login failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun handleLogin(email:String, password: String){
        if (email.isBlank()  || password.isBlank()){
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null){
                        getUserProfile(firebaseUser.uid)
                    }
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to main screen
                    // startActivity(Intent(this, MainActivity::class.java)); finish()
                } else {
                    Toast.makeText(this, "Authentication failed </3 : ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

        Toast.makeText(this, "Login clicked", Toast.LENGTH_SHORT).show()
    }

    private fun handleForgotPassword(){
        Toast.makeText(this, "Forgot password clicked", Toast.LENGTH_SHORT).show()
    }

    private fun handleCreateAccount(){
        Toast.makeText(this, "Create account clicked", Toast.LENGTH_SHORT).show()
    }

    private fun launchGoogleSignIn() {
        startActivityForResult(googleClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Sign-In success!", Toast.LENGTH_SHORT).show()
                    // TODO: Navigate to main screen
                    // startActivity(Intent(this, MainActivity::class.java)); finish()
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Sign-In failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getUserProfile(uid: String) {
        val userRef = database.reference.child("users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                updateUserLastLogin(uid)
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                // TODO: Navigate to MainActivity
                // startActivity(Intent(this, MainActivity::class.java))
                // finish()
            } else {
                Toast.makeText(this, "Profile not found. Please sign up first.", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserLastLogin(uid: String) {
        database.reference.child("users").child(uid)
            .child("updatedAt")
            .setValue(System.currentTimeMillis())
    }
}
