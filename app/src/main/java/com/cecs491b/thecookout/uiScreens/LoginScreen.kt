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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cecs491b.thecookout.R
import com.cecs491b.thecookout.support.openSupportEmail
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.LightGreyText
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

/* ---------------- GOOGLE BUTTON ---------------- */
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

/* ---------------- TIKTOK BUTTON ---------------- */
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
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
    ) { Text(text) }
}

/* ---------------- LOGIN SCREEN ---------------- */
@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onNavigateToForgot: () -> Unit = {},
    onNavigateToSignup: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
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

            /* ---------------- HEADER ---------------- */
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

                // LIGHTER overlay so logo is visible
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x33FFFFFF),
                                    Color(0x66FFFFFF),
                                    Color(0xAAFB923C),
                                    Color(0xFFFB923C)
                                ),
                                startY = 150f
                            )
                        )
                )

                // LOGO + TITLE
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(90.dp),   // ⭐ bigger logo
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        tonalElevation = 6.dp,
                        shadowElevation = 6.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cookout_logo),
                            contentDescription = "The Cookout Logo",
                            modifier = Modifier.padding(10.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("The Cookout", color = Color.White, fontSize = 30.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Share your recipes!", color = Color(0xFFFFE3C2), fontSize = 15.sp)
                }
            }

            /* ---------------- FORM CARD ---------------- */
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
                    Text("Welcome back!", fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to your account to continue cooking",
                        fontSize = 13.sp,
                        color = LightGreyText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- Email ---------------- */
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    /* ---------------- Password ---------------- */
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password", color = LightGreyText) },
                        singleLine = true,
                        visualTransformation =
                            if (pwVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { pwVisible = !pwVisible }) {
                                Icon(
                                    imageVector =
                                        if (pwVisible) Icons.Filled.VisibilityOff
                                        else Icons.Filled.Visibility,
                                    contentDescription =
                                        if (pwVisible) "Hide password" else "Show password"
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text("Remember me", fontSize = 14.sp)
                        }

                        Text(
                            "Forgot password?",
                            color = CookoutOrange,
                            modifier = Modifier.clickable { onNavigateToForgot() },
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    /* ---------------- Sign In Button ---------------- */
                    Button(
                        onClick = { onLoginClick(email, password) },
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
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else Text("Sign In")
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(Modifier.weight(1f))
                        Text("  or  ", color = LightGreyText)
                        HorizontalDivider(Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    GoogleButton(
                        onClick = onGoogleSignInClick,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(12.dp))

                    TikTokSignInButton(enabled = !isLoading)

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onNavigateToSignup,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFDADCE0))
                    ) {
                        Text("Don't have an account? ", color = Color(0xFF5F6368))
                        Text("Sign up", color = CookoutOrange)
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            openSupportEmail(
                                context = ctx,
                                subjectExtra = "Login help",
                                bodyExtra = "Describe what happened, steps to reproduce, screenshots."
                            )
                        }
                    ) {
                        Text("Contact Support", color = CookoutOrange)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "© 2025 The Cookout. All rights reserved.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = LightGreyText,
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
    TheCookoutTheme {
        LoginScreen()
    }
}
