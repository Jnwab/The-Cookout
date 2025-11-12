package com.cecs491b.thecookout.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cecs491b.thecookout.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed class AuthState { data object Idle:AuthState(); data object Loading:AuthState(); data object Success:AuthState(); data class Error(val message:String):AuthState() }
sealed class NavigationEvent { data object NavigateToProfile:NavigationEvent(); data object NavigateToLogin:NavigationEvent(); data object NavigateToSignup:NavigationEvent(); data object NavigateToForgotPassword:NavigationEvent(); data object NavigateToPhoneAuth:NavigationEvent() }

data class LoginUiState(val email:String="", val password:String="", val rememberMe:Boolean=false, val passwordVisible:Boolean=false, val authState:AuthState=AuthState.Idle)
data class SignupUiState(val email:String="", val password:String="", val displayName:String="", val phoneNumber:String="", val authState:AuthState=AuthState.Idle)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val http: OkHttpClient,
    @Named("AUTH_BASE_URL") private val authBaseUrl: String
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginUiState())
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    private val _signupUiState = MutableStateFlow(SignupUiState())
    val signupUiState: StateFlow<SignupUiState> = _signupUiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("Please enter email and password"))
            return
        }
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Loading)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { handleUserLogin(it.uid) } ?: run {
                    _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("User not found"))
                }
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("Authentication failed: ${e.message}"))
            }
        }
    }

    fun verifyGoogleToken(idToken: String) {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Loading)
            try {
                val customToken = verifyOnServer("/googleVerify", idToken)
                val result = auth.signInWithCustomToken(customToken).await()
                result.user?.let { handleUserLogin(it.uid) } ?: run {
                    _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("No Firebase user"))
                }
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("Google sign-in failed: ${e.message}"))
            }
        }
    }

    fun signInWithCustomToken(token: String) {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Loading)
            try {
                val result = auth.signInWithCustomToken(token).await()
                result.user?.let { handleUserLogin(it.uid) } ?: run {
                    _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("TikTok login failed"))
                }
            } catch (e: Exception) {
                _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("TikTok login failed: ${e.message}"))
            }
        }
    }

    fun signupWithEmail(email:String, password:String, displayName:String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Error("Please fill out all fields")); return
        }
        if (password.length < 10) {
            _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Error("Password must be at least 10 characters")); return
        }
        viewModelScope.launch {
            _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Loading)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let {
                    val user = User(
                        uid = it.uid, email = email, displayName = displayName, phoneNumber = "",
                        photoUrl = "", provider = "email", createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                    )
                    saveUserToDatabase(user)
                } ?: run {
                    _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Error("Failed to create user"))
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("already in use", true) == true -> "This email is already registered. Please login instead."
                    e.message?.contains("badly formatted", true) == true -> "Invalid email format"
                    e.message?.contains("weak password", true) == true -> "Password is too weak"
                    else -> "Signup failed: ${e.message}"
                }
                _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Error(msg))
            }
        }
    }

    private suspend fun handleUserLogin(uid: String) {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                updateUserLastLogin(uid)
                _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Success)
                _navigationEvent.value = NavigationEvent.NavigateToProfile
            } else {
                auth.currentUser?.let { createUserProfile(it) } ?: run {
                    _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("User not found"))
                }
            }
        } catch (e: Exception) {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("Failed to load profile: ${e.message}"))
        }
    }

    private suspend fun createUserProfile(u: FirebaseUser) {
        try {
            val user = User(
                uid = u.uid, email = u.email ?: "", displayName = u.displayName ?: "User", phoneNumber = "",
                photoUrl = "", provider = u.providerData.firstOrNull()?.providerId ?: "email",
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
            firestore.collection("users").document(u.uid).set(user).await()
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Success)
            _navigationEvent.value = NavigationEvent.NavigateToProfile
        } catch (e: Exception) {
            _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Error("Failed to create profile: ${e.message}"))
        }
    }

    private suspend fun saveUserToDatabase(user: User) {
        try {
            firestore.collection("users").document(user.uid).set(user).await()
            _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Success)
            _navigationEvent.value = NavigationEvent.NavigateToLogin
        } catch (e: Exception) {
            _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Error("Failed to save profile: ${e.message}"))
        }
    }

    private suspend fun updateUserLastLogin(uid: String) {
        try { firestore.collection("users").document(uid).update("updatedAt", System.currentTimeMillis()).await() }
        catch (e: Exception) { Log.e("AuthViewModel", "Failed to update last login", e) }
    }

    private suspend fun verifyOnServer(path: String, idToken: String): String =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val body = """{"idToken":"$idToken"}""".toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url("$authBaseUrl$path").post(body).build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("Server verification failed: ${resp.message}")
                val text = resp.body?.string() ?: throw Exception("Empty response from server")
                JSONObject(text).getString("customToken")
            }
        }

    fun navigateToForgotPassword(){ _navigationEvent.value = NavigationEvent.NavigateToForgotPassword }
    fun navigateToSignup(){ _navigationEvent.value = NavigationEvent.NavigateToSignup }
    fun navigateToLogin(){ _navigationEvent.value = NavigationEvent.NavigateToLogin }
    fun clearNavigationEvent(){ _navigationEvent.value = null }
    fun clearAuthState(){ _loginUiState.value = _loginUiState.value.copy(authState = AuthState.Idle); _signupUiState.value = _signupUiState.value.copy(authState = AuthState.Idle) }
}
