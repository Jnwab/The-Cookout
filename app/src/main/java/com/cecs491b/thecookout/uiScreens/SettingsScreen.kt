package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cecs491b.thecookout.ui.theme.CookoutOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPrivacyCenterClick: () -> Unit = {},
    onDevicePermissionsClick: () -> Unit = {},
    onLinkedAccountsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            SettingsItem(
                text = "Edit Profile",
                icon = Icons.Default.Person,
                isSelected = true,
                onClick = onEditProfileClick
            )

            SettingsItem(
                text = "Privacy Center",
                icon = Icons.Default.Lock,
                onClick = onPrivacyCenterClick
            )

            SettingsItem(
                text = "Device Permissions",
                icon = Icons.Default.Devices,
                onClick = onDevicePermissionsClick
            )

            SettingsItem(
                text = "Linked Accounts",
                icon = Icons.Default.Link,
                onClick = onLinkedAccountsClick
            )

            SettingsItem(
                text = "Help & Support",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                onClick = onHelpSupportClick
            )

            SettingsItem(
                text = "About",
                icon = Icons.Default.Info,
                onClick = onAboutClick
            )

            Spacer(Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogoutClick() }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log Out",
                    tint = Color(0xFFD32F2F)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Log Out",
                    color = Color(0xFFD32F2F),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(CookoutOrange, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}
