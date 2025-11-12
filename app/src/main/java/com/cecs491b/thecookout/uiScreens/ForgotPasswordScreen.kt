package com.cecs491b.thecookout.uiScreens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cecs491b.thecookout.support.openSupportEmail
import com.cecs491b.thecookout.viewmodels.ForgotPasswordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    vm: ForgotPasswordViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val ctx = LocalContext.current

    // toast errors/success
    LaunchedEffect(ui.error, ui.sent) {
        ui.error?.let { Toast.makeText(ctx, it, Toast.LENGTH_LONG).show() }
        if (ui.sent) Toast.makeText(ctx, "Reset link sent. Check your inbox.", Toast.LENGTH_LONG).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Forgot Password") })
            HorizontalDivider(thickness = 4.dp, color = Color.Black)
        }
    ) { inner ->
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
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
                        value = ui.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { vm.sendReset() },
                        enabled = vm.canSend(),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                    ) {
                        if (ui.loading) {
                            CircularProgressIndicator()
                        } else if (ui.remainingSeconds == 0L) {
                            Text("Send Reset Link")
                        } else {
                            val m = ui.remainingSeconds / 60
                            val s = ui.remainingSeconds % 60
                            Text("Resend in %d:%02d".format(m, s))
                        }
                    }

                    TextButton(onClick = onClose) { Text("Back to Login") }
                }
            }

            // Contact support
            TextButton(
                onClick = {
                    openSupportEmail(
                        context = ctx,
                        subjectExtra = "Password reset help",
                        bodyExtra = "I tried resetting my password but…"
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Need help? Contact Support") }
        }
    }
}
