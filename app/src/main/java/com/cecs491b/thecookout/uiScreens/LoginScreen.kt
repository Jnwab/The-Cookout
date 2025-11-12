package com.cecs491b.thecookout.uiScreens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.support.openSupportEmail
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.LightGreyText
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

@Composable
private fun GoogleButton(
    text: String = "Continue with Google",
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(text)
        }
    }
}

@Composable
private fun TikTokSignInButton(
    modifier: Modifier = Modifier,
    text: String = "Continue with TikTok",
    startUrl: String = "https://lakita-frothiest-meanderingly.ngrok-free.dev/tiktokStart",
    enabled: Boolean = true
) {
    val context = LocalContext.current
    Button(
        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(startUrl))) },
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF000000),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) { Text(text) }
    }
}

@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onNavigateToForgot: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
) {
    val ctx = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var pwVisible by rememberSaveable { mutableStateOf(false) }

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
            // Header image + overlay + title
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
                    Text("The Cookout", color = Color.White, fontSize = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Share your recipes!", color = Color(0xFFFFE3C2), fontSize = 14.sp)
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
                    Text("Welcome back!", fontSize = 20.sp, color = Color(0xFF1F1F1F))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to your account to continue cooking",
                        color = LightGreyText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        placeholder = { Text("chef@cookout.com", color = LightGreyText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password with eye toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password", color = LightGreyText) },
                        singleLine = true,
                        enabled = !isLoading,
                        visualTransformation = if (pwVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pwVisible = !pwVisible }, enabled = !isLoading) {
                                Icon(
                                    imageVector = if (pwVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                    contentDescription = if (pwVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Remember me + Forgot password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                enabled = !isLoading
                            )
                            Text("Remember me", fontSize = 14.sp)
                        }
                        Text(
                            "Forgot password?",
                            color = CookoutOrange,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable(enabled = !isLoading) { onNavigateToForgot() }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Primary Sign In
                    Button(
                        onClick = { onLoginClick(email, password) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CookoutOrange,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Sign In")
                        }
                    }

                    // Divider
                    Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text("  or  ", color = LightGreyText)
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))

                    // Google
                    GoogleButton(
                        onClick = onGoogleSignInClick,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // TikTok (kept self-contained; opens browser to your start URL)
                    TikTokSignInButton(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(12.dp))

                    // Sign up
                    OutlinedButton(
                        onClick = onNavigateToSignup,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0))
                    ) {
                        Text("Don't have an account? ", color = Color(0xFF5F6368))
                        Text("Sign up", color = CookoutOrange)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Contact Support
                    TextButton(
                        onClick = {
                            openSupportEmail(
                                context = ctx,
                                subjectExtra = "Login help",
                                bodyExtra = "Describe what happened, steps to reproduce, and any screenshots."
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Contact Support", color = CookoutOrange) }

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
private fun LoginScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        LoginScreen()
    }
}
