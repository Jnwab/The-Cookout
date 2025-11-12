package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val vm: AuthViewModel by viewModels()
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
                vm.verifyGoogleToken(idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        // Google sign-in client
        googleClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build()
        )

        enableEdgeToEdge()
        setContent {
            TheCookoutTheme {
                val uiState by vm.loginUiState.collectAsState()
                val navEvent by vm.navigationEvent.collectAsState()

                // Navigate when VM emits events
                LaunchedEffect(navEvent) {
                    when (navEvent) {
                        is NavigationEvent.NavigateToProfile -> {
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish()
                            vm.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToForgotPassword -> {
                            startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
                            vm.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToSignup -> {
                            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
                            vm.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToPhoneAuth -> {
                            startActivity(Intent(this@LoginActivity, PhoneAuthActivity::class.java))
                            vm.clearNavigationEvent()
                        }
                        else -> Unit
                    }
                }

                // Toast errors / success feedback
                LaunchedEffect(uiState.authState) {
                    when (val s = uiState.authState) {
                        is AuthState.Error -> {
                            Toast.makeText(this@LoginActivity, s.message, Toast.LENGTH_LONG).show()
                            vm.clearAuthState()
                        }
                        is AuthState.Success -> {
                            Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }

                Surface(Modifier.fillMaxSize()) {
                    LoginScreen(
                        isLoading = uiState.authState is AuthState.Loading,
                        onLoginClick = { email, password -> vm.loginWithEmail(email, password) },
                        onNavigateToForgot = { vm.navigateToForgotPassword() },
                        onNavigateToSignup = { vm.navigateToSignup() },
                        onGoogleSignInClick = { startGoogleSignIn() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        val isPreferred = data.scheme == "cookout" && data.host == "tiktok" && data.path == "/callback"
        val isLegacy = data.scheme == "cookout" && data.host == "auth" && data.path == "/callback"
        if (!isPreferred && !isLegacy) return

        val token = data.getQueryParameter("customToken")
            ?: data.getQueryParameter("token")

        if (!token.isNullOrBlank()) {
            vm.signInWithCustomToken(token)
        } else {
            Toast.makeText(this, "Missing token from TikTok callback.", Toast.LENGTH_LONG).show()
        }
    }

    private fun startGoogleSignIn() {
        googleSignInLauncher.launch(googleClient.signInIntent)
    }
}
