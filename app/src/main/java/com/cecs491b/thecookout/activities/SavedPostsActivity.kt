// activities/SavedPostsActivity.kt
package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cecs491b.thecookout.uiScreens.BottomNavBar
import com.cecs491b.thecookout.uiScreens.SavedPostsScreen
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme

class SavedPostsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(
                                current = "saved",
                                onHomeClick = {
                                    startActivity(Intent(this, HomeActivity::class.java))
                                },
                                onSavedClick = { /* already on Saved */ },
                                onAlertsClick = {
                                    startActivity(Intent(this, AlertsActivity::class.java))
                                },
                                onProfileClick = {
                                    startActivity(Intent(this, ProfileActivity::class.java))
                                }
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(
                                onClick = {
                                    startActivity(
                                        Intent(this, RecipeCreationActivity::class.java)
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
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            SavedPostsScreen()
                        }
                    }
                }
            }
        }
    }
}
