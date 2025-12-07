package com.cecs491b.thecookout.uiScreens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavBar(
    current: String,
    onHomeClick: () -> Unit,
    onSavedClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {

        // Home
        NavigationBarItem(
            selected = current == "home",
            onClick = onHomeClick,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            alwaysShowLabel = true
        )

        // Saved
        NavigationBarItem(
            selected = current == "saved",
            onClick = onSavedClick,
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved") },
            label = { Text("Saved") },
            alwaysShowLabel = true
        )

        // Alerts
        NavigationBarItem(
            selected = current == "alerts",
            onClick = onAlertsClick,
            icon = { Icon(Icons.Filled.Notifications, contentDescription = "Alerts") },
            label = { Text("Alerts") },
            alwaysShowLabel = true
        )

        // Profile
        NavigationBarItem(
            selected = current == "profile",
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            alwaysShowLabel = true
        )
    }
}
