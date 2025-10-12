package com.cecs491b.thecookout.activities


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cecs491b.thecookout.uiScreens.PhoneAuthScreen
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import java.util.concurrent.TimeUnit


class PhoneAuthActivity : ComponentActivity() {

        private lateinit var auth: FirebaseAuth
        private var verificationId: String? = null
        private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
        private val timeout = 60L

        private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification succeeded
                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Auto-verification successful!",
                    Toast.LENGTH_SHORT
                ).show()
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Verification failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(
                            this@PhoneAuthActivity,
                            "Invalid phone number format",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is FirebaseAuthMissingActivityForRecaptchaException -> {
                        Toast.makeText(
                            this@PhoneAuthActivity,
                            "reCAPTCHA verification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@PhoneAuthActivity.verificationId = verificationId
                this@PhoneAuthActivity.resendToken = token

                Toast.makeText(
                    this@PhoneAuthActivity,
                    "Verification code sent!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            auth = Firebase.auth

            setContent {
                PhoneAuthScreen(
                    onSendCode = { phoneNumber ->
                        sendVerificationCode(phoneNumber)
                    },
                    onVerifyCode = { code ->
                        verifyCode(code)
                    },
                    onResendCode = { phoneNumber ->
                        resendVerificationCode(phoneNumber)
                    },
                    onBackToLogin = {
                        finish()
                    }
                )
            }
        }

        private fun sendVerificationCode(phoneNumber: String) {
            // Validate phone number format
            if (!phoneNumber.startsWith("+")) {
                Toast.makeText(
                    this,
                    "Phone number must start with + and country code",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeout, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        private fun resendVerificationCode(phoneNumber: String) {
            val token = resendToken

            if (token == null) {
                Toast.makeText(this, "Cannot resend yet", Toast.LENGTH_SHORT).show()
                return
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeout, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

        private fun verifyCode(code: String) {
            val verificationId = this.verificationId

            if (verificationId == null) {
                Toast.makeText(this, "Please request code first", Toast.LENGTH_SHORT).show()
                return
            }

            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }

        private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        Toast.makeText(
                            this,
                            "Phone verified successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to MainActivity
                        // startActivity(Intent(this, MainActivity::class.java))
                        // finish()

                        // For now, just go back
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Verification failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }