package com.cecs491b.thecookout.uiScreens

import android.graphics.Paint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.cecs491b.thecookout.support.openSupportEmail
import androidx.compose.material3.TextButton

import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onSendReset: (String) -> Unit = {},
    onClose: () -> Unit = {}
) {
    val vm: ForgotPasswordViewModel = viewModel()
    val remaining by vm.remainingSeconds.collectAsState()
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth

    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
            )
            HorizontalDivider(
                modifier = Modifier,
                thickness = 4.dp,
                color = Color.Black
            )
        }
    ) { inner ->

        Column(
            verticalArrangement = Arrangement.Center ,modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState(),)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Enter the email you used to sign up. We’ll send a password reset link.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

            val canSend = remaining == 0L && email.isNotBlank()

            Button(
                onClick = {
                    vm.sendReset(email, auth) { ok, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth()
            ) {
                if (remaining == 0L) {
                    Text("Send Reset Link")
                } else {
                    val m = remaining / 60
                    val s = remaining % 60
                    Text("Resend in %d:%02d".format(m, s))
                }
            }

                    TextButton(onClick = onClose) {
                        Text("Back to Login")
                    }
                }
            }

            // --- Contact Support ---
            TextButton(
                onClick = {
                    openSupportEmail(
                        context = context,
                        subjectExtra = "Password reset help",
                        bodyExtra = "I tried resetting my password but…"
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Need help? Contact Support")
            }
        }
    }
}
