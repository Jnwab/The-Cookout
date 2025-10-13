package com.cecs491b.thecookout.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.uiScreens.ForgotPasswordScreen
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ForgotPasswordScreen(
                        onSendReset = { email ->
                            if (email.isBlank()) {
                                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
                                return@ForgotPasswordScreen
                            }
                            auth.sendPasswordResetEmail(email.trim())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Password reset email sent. Check your inbox.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        e.localizedMessage ?: "Failed to send reset email.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        },
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}
