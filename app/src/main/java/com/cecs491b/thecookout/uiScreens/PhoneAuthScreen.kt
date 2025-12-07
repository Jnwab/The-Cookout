package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.DarkerOrange

@Composable
fun PhoneAuthScreen(
    onSendCode: (String) -> Unit = {},
    onVerifyCode: (String) -> Unit = {},
    onResendCode: (String) -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Phone Verification",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DarkerOrange,
                        CookoutOrange,
                        DarkerOrange
                    )
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "For testing, use: +11234567890 with code 123456",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Text(
            text = if (!codeSent)
                "Enter your phone number to receive a verification code"
            else
                "Enter the 6-digit code sent to $phoneNumber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!codeSent) {
            // Phone number input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+1234567890") },
                supportingText = {
                    Text("Include country code (e.g., +1 for US)")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (phoneNumber.isNotBlank()) {
                        onSendCode(phoneNumber)
                        codeSent = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.isNotBlank()
            ) {
                Text("Send Verification Code")
            }
        } else {
            // Verification code input
            OutlinedTextField(
                value = verificationCode,
                onValueChange = {
                    if (it.length <= 6) {
                        verificationCode = it
                    }
                },
                label = { Text("Verification Code") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (verificationCode.length == 6) {
                        onVerifyCode(verificationCode)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = verificationCode.length == 6
            ) {
                Text("Verify Code")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        codeSent = false
                        verificationCode = ""
                    }
                ) {
                    Text("Change Number")
                }

                TextButton(
                    onClick = { onResendCode(phoneNumber) }
                ) {
                    Text("Resend Code")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login", color = CookoutOrange)
        }
    }
}