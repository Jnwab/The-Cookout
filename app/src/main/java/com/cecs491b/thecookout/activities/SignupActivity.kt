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
import com.google.firebase.storage.storage
import com.cecs491b.thecookout.uiScreens.SignupScreen
import com.cecs491b.thecookout.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class SignupActivity: ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle? ){
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.firestore

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()){
                    SignupScreen(
                        onSignupClick = {email, password, displayName ->
                            handleSignup(email, password, displayName)
                        },
                        onBackToLoginClick = {
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        },
                        onGoogleSignInClick = { launchGoogleLink() }
                    )
                }
            }
        }
    }

    private fun launchGoogleLink(){
        // pass rn
    }

    private fun handleSignup(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 10) {
            Toast.makeText(this, "Password must be at least 10 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = displayName,
                            phoneNumber = "",
                            photoUrl = "",
                            provider = "email",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )

                        saveUserToDatabase(user)
                    }
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("email address is already in use") == true ->
                            "This email is already registered. Please login instead."
                        task.exception?.message?.contains("badly formatted") == true ->
                            "Invalid email format"
                        task.exception?.message?.contains("weak password") == true ->
                            "Password is too weak"
                        else -> "Signup failed: ${task.exception?.message}"
                    }
                    Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToDatabase(user: User) {
        database.collection("users").document(user.uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to save profile: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}