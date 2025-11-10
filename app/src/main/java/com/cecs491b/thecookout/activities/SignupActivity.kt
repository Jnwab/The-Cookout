package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.cecs491b.thecookout.uiScreens.SignupScreen
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

class SignupActivity : ComponentActivity() {
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
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(Firebase.auth, Firebase.firestore)
        )[AuthViewModel::class.java]

        // Setup Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        setContent {
            TheCookoutTheme {
                val uiState by viewModel.signupUiState.collectAsState()
                val navigationEvent by viewModel.navigationEvent.collectAsState()

                // Handle navigation events
                LaunchedEffect(navigationEvent) {
                    when (navigationEvent) {
                        is NavigationEvent.NavigateToLogin -> {
                            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                            finish()
                            viewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToProfile -> {
                            startActivity(Intent(this@SignupActivity, ProfileActivity::class.java))
                            finish()
                            viewModel.clearNavigationEvent()
                        }
                        else -> {}
                    }
                }

                // Handle auth state changes
                LaunchedEffect(uiState.authState) {
                    when (val state = uiState.authState) {
                        is AuthState.Error -> {
                            Toast.makeText(this@SignupActivity, state.message, Toast.LENGTH_LONG).show()
                            viewModel.clearAuthState()
                        }
                        is AuthState.Success -> {
                            Toast.makeText(this@SignupActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    SignupScreen(
                        isLoading = uiState.authState is AuthState.Loading,
                        onSignupClick = { email, password, displayName ->
                            viewModel.signupWithEmail(email, password, displayName)
                        },
                        onBackToLogin = {
                            viewModel.navigateToLogin()
                        },
                        onGoogleSignInClick = {
                            googleSignInLauncher.launch(googleClient.signInIntent)
                        }
                    )
                }
            }
        }
    }
}