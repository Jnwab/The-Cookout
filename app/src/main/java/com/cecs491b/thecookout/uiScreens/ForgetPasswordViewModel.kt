package com.cecs491b.thecookout.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ForgotUiState(
    val email: String = "",
    val remainingSeconds: Long = 0L,
    val loading: Boolean = false,
    val sent: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val PREFS = "auth_prefs"
        private const val KEY_LAST_RESET_MS = "last_pw_reset_ms"
        private const val COOLDOWN_MS = 60_000L  // 60 seconds (adjust as you like)
    }

    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _ui = MutableStateFlow(ForgotUiState())
    val ui: StateFlow<ForgotUiState> = _ui

    private var ticker: Job? = null

    init {
        // Restore any running cooldown when screen opens
        val last = prefs.getLong(KEY_LAST_RESET_MS, 0L)
        val leftMs = (last + COOLDOWN_MS) - System.currentTimeMillis()
        if (leftMs > 0) startTimer(leftMs)
    }

    fun onEmailChange(v: String) {
        _ui.value = _ui.value.copy(email = v, error = null)
    }

    fun canSend(): Boolean = _ui.value.remainingSeconds == 0L && _ui.value.email.isNotBlank() && !_ui.value.loading

    fun sendReset() = viewModelScope.launch {
        val leftMs = timeLeftMs()
        if (leftMs > 0) {
            _ui.value = _ui.value.copy(
                error = "Please wait ${format(leftMs / 1000)} before trying again."
            )
            return@launch
        }

        val email = _ui.value.email.trim()
        if (email.isEmpty()) {
            _ui.value = _ui.value.copy(error = "Please enter your email.")
            return@launch
        }

        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            auth.sendPasswordResetEmail(email).await()
            // Start cooldown
            prefs.edit().putLong(KEY_LAST_RESET_MS, System.currentTimeMillis()).apply()
            startTimer(COOLDOWN_MS)
            _ui.value = _ui.value.copy(loading = false, sent = true)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(
                loading = false,
                error = e.localizedMessage ?: "Failed to send reset email."
            )
        }
    }

    private fun timeLeftMs(): Long {
        val last = prefs.getLong(KEY_LAST_RESET_MS, 0L)
        return (last + COOLDOWN_MS) - System.currentTimeMillis()
    }

    private fun startTimer(durationMs: Long) {
        ticker?.cancel()
        ticker = viewModelScope.launch {
            var left = durationMs / 1000
            while (left >= 0) {
                _ui.value = _ui.value.copy(remainingSeconds = left)
                delay(1_000)
                left--
            }
            _ui.value = _ui.value.copy(remainingSeconds = 0)
        }
    }

    private fun format(s: Long): String {
        val m = s / 60
        val sec = s % 60
        return "%d:%02d".format(m, sec)
    }
}
