package com.cecs491b.thecookout.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.uiScreens.LoginScreen
import com.cecs491b.thecookout.viewmodels.AuthState
import com.cecs491b.thecookout.viewmodels.AuthViewModel
import com.cecs491b.thecookout.viewmodels.NavigationEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class LoginActivity : ComponentActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var googleClient: GoogleSignInClient

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
                viewModel.verifyGoogleToken(idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("AUTH", "Google sign-in failed", e)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Firebase emulators if in debug mode
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
            Firebase.auth.useEmulator("10.0.2.2", 9100)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(Firebase.auth, Firebase.firestore)
        )[AuthViewModel::class.java]

        // Handle deep link
        handleDeepLink(intent)

        // Setup Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            TheCookoutTheme {
                val uiState by viewModel.loginUiState.collectAsState()
                val navigationEvent by viewModel.navigationEvent.collectAsState()

                // Handle navigation events
                LaunchedEffect(navigationEvent) {
                    when (navigationEvent) {
                        is NavigationEvent.NavigateToProfile -> {
                            startActivity(Intent(this@LoginActivity, ProfileActivity::class.java))
                            finish()
                            viewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToForgotPassword -> {
                            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
                            viewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToSignup -> {
                            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
                            viewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToPhoneAuth -> {
                            startActivity(Intent(this@LoginActivity, PhoneAuthActivity::class.java))
                            viewModel.clearNavigationEvent()
                        }
                        else -> {}
                    }
                }

                // Handle auth state changes
                LaunchedEffect(uiState.authState) {
                    when (val state = uiState.authState) {
                        is AuthState.Error -> {
                            Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearAuthState()
                        }
                        is AuthState.Success -> {
                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        isLoading = uiState.authState is AuthState.Loading,
                        onLoginClick = { email, password ->
                            viewModel.loginWithEmail(email, password)
                        },
                        onForgotPasswordClick = {
                            viewModel.navigateToForgotPassword()
                        },
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(googleClient.signInIntent)
                        },
                        onSignupClick = {
                            viewModel.navigateToSignup()
                        },
                        onPhoneAuthClick = {
                            viewModel.navigateToPhoneAuth()
                        }
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

        viewModel.signInWithCustomToken(token)
    }
}

// ViewModel Factory
class AuthViewModelFactory(
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(auth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}