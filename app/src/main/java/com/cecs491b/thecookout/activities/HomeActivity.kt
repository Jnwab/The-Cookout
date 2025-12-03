// activities/HomeActivity.kt
package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cecs491b.thecookout.uiScreens.BottomNavBar
import com.cecs491b.thecookout.uiScreens.HomeScreen
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import androidx.compose.foundation.layout.padding

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    HomeScaffold()
                }
            }
        }
    }

    @Composable
    private fun HomeScaffold() {
        Scaffold(
            bottomBar = {
                BottomNavBar(
                    current = "home",
                    onHomeClick = { /* already on home */ },
                    onSavedClick = {
                        startActivity(Intent(this@HomeActivity, SavedPostsActivity::class.java))
                    },
                    onAlertsClick = {
                        startActivity(Intent(this@HomeActivity, AlertsActivity::class.java))
                    },
                    onProfileClick = {
                        startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        startActivity(
                            Intent(
                                this@HomeActivity,
                                RecipeCreationActivity::class.java
                            )
                        )
                    },
                    containerColor = CookoutOrange,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Create Recipe"
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            HomeScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}
