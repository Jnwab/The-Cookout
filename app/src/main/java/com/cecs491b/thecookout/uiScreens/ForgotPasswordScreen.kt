package com.cecs491b.thecookout.uiScreens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.support.openSupportEmail
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.LightGreyText
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.viewmodels.ForgotPasswordViewModel

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFB923C), Color(0xFFFDBA74))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header image + overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.familycooking),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x1A7C2D12),
                                    Color(0x4DFB923C),
                                    Color(0xCCFB923C),
                                    Color(0xFFFB923C)
                                ),
                                startY = 300f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cookout_logo),
                            contentDescription = "The Cookout Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Password Reset", color = Color.White, fontSize = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("We'll help you get back in", color = Color(0xFFFFE3C2), fontSize = 14.sp)
                }
            }

            // Card section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Forgot your password?", fontSize = 20.sp, color = Color(0xFF1F1F1F))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Enter the email you used to sign up. We'll send a password reset link.",
                        color = LightGreyText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // Email input field
                    OutlinedTextField(
                        value = ui.email,
                        onValueChange = vm::onEmailChange,
                        label = { Text("Email") },
                        placeholder = { Text("chef@cookout.com", color = LightGreyText) },
                        singleLine = true,
                        enabled = !ui.loading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Send Reset Link Button
                    Button(
                        onClick = { vm.sendReset() },
                        enabled = vm.canSend(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CookoutOrange,
                            contentColor = Color.White
                        )
                    ) {
                        if (ui.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else if (ui.remainingSeconds == 0L) {
                            Text("Send Reset Link")
                        } else {
                            val m = ui.remainingSeconds / 60
                            val s = ui.remainingSeconds % 60
                            Text("Resend in %d:%02d".format(m, s))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Back to Login
                    TextButton(
                        onClick = onClose,
                        enabled = !ui.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Login", color = CookoutOrange)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Contact Support
                    TextButton(
                        onClick = {
                            openSupportEmail(
                                context = ctx,
                                subjectExtra = "Password reset help",
                                bodyExtra = "I tried resetting my password but…"
                            )
                        },
                        enabled = !ui.loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Need help? Contact Support", color = CookoutOrange)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "© 2025 The Cookout. All rights reserved.",
                        color = LightGreyText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(150.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        ForgotPasswordScreen()
    }
}