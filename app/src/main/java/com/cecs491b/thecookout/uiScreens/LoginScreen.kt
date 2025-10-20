package com.cecs491b.thecookout.uiScreens

import com.cecs491b.thecookout.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.DarkerOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.cecs491b.thecookout.activities.ForgotPasswordActivity

@Composable
private fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    text: String = "Sign in with Google",
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledContentColor = Color(0x61000000)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
            .height(48.dp)
            .width(190.dp)
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
                modifier = Modifier
                    .size(30.dp)
                    .padding(end = 12.dp)
            )
            Text(text)
        }
    }
}

@Composable
private fun TikTokSignInButton(
    modifier: Modifier = Modifier,
    text: String = "Continue with TikTok",
    startUrl: String = "https://lakita-frothiest-meanderingly.ngrok-free.dev/tiktokStart"
) {
    val context = LocalContext.current
    Button(
        onClick = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(startUrl))
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF000000),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
            .height(48.dp)
            .width(190.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // If add a TikTok icon later, put it here similar to Google.
            Text(text)
        }
    }
}

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    onSignupClick: () -> Unit = {},
    onPhoneAuthClick: () -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFB923C),
                        Color(0xFFFDBA74),
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with backdrop image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(288.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.familycooking),
                    contentDescription = "Food Brings Family together",
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
                            .size(64.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.cookout_logo),
                            contentDescription = "The Cookout Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "The Cookout",
                        fontSize = 30.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Connect through Food!",
                        fontSize = 18.sp,
                        color = Color(0xFFFED7AA)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome back!",
                        fontSize = 20.sp,
                        color = Color(0xFFEA580C)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Forgot Password?",
                            color = Color(0xFFEA580C),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onForgotPasswordClick() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "New Here? Create Account",
                            color = Color(0xFFEA580C),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onSignupClick() }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { onLoginClick(email, password) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .width(200.dp)
                    ) {
                        Text("Log in")
                    }

                    Spacer(Modifier.height(12.dp))

                    GoogleSignInButton(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                    )

                    Spacer(Modifier.height(12.dp))

                    // ---- TikTok button UNDER Google ----
                    TikTokSignInButton()

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onPhoneAuthClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Phone Number")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
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
