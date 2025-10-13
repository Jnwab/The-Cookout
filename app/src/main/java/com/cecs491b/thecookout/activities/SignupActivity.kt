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
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import com.cecs491b.thecookout.uiScreens.SignupScreen
import com.cecs491b.thecookout.models.User


class SignupActivity: ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle? ){
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

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
                        }

                    )
                }
            }
        }
    }

    private fun handleSignup(email: String, password: String, displayName: String){
        if (email.isBlank() || password.isBlank() || displayName.isBlank()){
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 10) {
            Toast.makeText(this, "Password must be at least 10 characters", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Account creation successful.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                }
            }
    }

}