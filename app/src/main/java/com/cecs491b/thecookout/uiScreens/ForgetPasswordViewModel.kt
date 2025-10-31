package com.cecs491b.thecookout.uiScreens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private const val PREFS = "auth_prefs"
        private const val KEY_LAST_RESET_MS = "last_pw_reset_ms"

        // 🔧 Change this to the cooldown you want:
        // 60_000L = 60s, 5 * 60_000L = 5 minutes, etc.
        private const val COOLDOWN_MS = 60_000L
    }

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds

    private var ticker: Job? = null

    init {
        // Restore any running cooldown when the screen opens
        val last = prefs.getLong(KEY_LAST_RESET_MS, 0L)
        val leftMs = (last + COOLDOWN_MS) - System.currentTimeMillis()
        if (leftMs > 0) startTimer(leftMs)
    }

    fun canSend() = _remainingSeconds.value == 0L

    fun sendReset(
        email: String,
        auth: FirebaseAuth,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val leftMs = timeLeftMs()
        if (leftMs > 0) {
            onResult(false, "Please wait ${format(leftMs / 1000)} before trying again.")
            return
        }
        if (email.isBlank()) {
            onResult(false, "Please enter your email.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                // Start cooldown
                prefs.edit().putLong(KEY_LAST_RESET_MS, System.currentTimeMillis()).apply()
                startTimer(COOLDOWN_MS)
                onResult(true, "Reset link sent. Check your inbox.")
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Failed to send reset email.")
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
                _remainingSeconds.value = left
                delay(1_000)
                left--
            }
            _remainingSeconds.value = 0
        }
    }

    private fun format(s: Long): String {
        val m = s / 60
        val sec = s % 60
        return "%d:%02d".format(m, sec)
    }
}
