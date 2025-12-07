package com.cecs491b.thecookout.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.uiScreens.ForgotPasswordScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheCookoutTheme {
                ForgotPasswordScreen(onClose = { finish() })
            }
        }
    }
}
