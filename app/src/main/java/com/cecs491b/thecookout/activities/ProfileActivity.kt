package com.cecs491b.thecookout.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cecs491b.thecookout.BuildConfig
import com.cecs491b.thecookout.uiScreens.ProfileScreen
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import com.cecs491b.thecookout.uiScreens.BottomNavBar

class ProfileActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(
                                current = "profile",
                                onHomeClick = {
                                    startActivity(Intent(this, HomeActivity::class.java))
                                },
                                onSavedClick = {
                                    startActivity(Intent(this, SavedPostsActivity::class.java))
                                },
                                onProfileClick = {}
                            )
                        }
                    ) { pad ->
                        Box(Modifier.padding(pad)) {
                            ProfileContent()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileContent() {
        var displayName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var provider by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }


        LaunchedEffect(Unit) {
            loadUserProfile { name, mail, phone, prov ->
                displayName = name
                email = mail
                phoneNumber = phone
                provider = prov
                isLoading = false
            }
        }

        ProfileScreen(
            displayName = displayName,
            email = email,
            phoneNumber = phoneNumber,
            provider = provider,
            isLoading = isLoading,
            //onChangeAvatarClick = {
            //    pickAvatar.launch(
            //        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            //    )
            //},*/
            onEditProfileClick = {
                startActivity(Intent(this@ProfileActivity, EditProfileActivity::class.java))
            },
            onSignOutClick = {
                handleSignOut()
            }
        )
    }

    private fun loadUserProfile(onComplete: (String, String, String, String) -> Unit) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val email = user.email ?: ""
        val displayNameFromAuth = user.displayName ?: ""

        db.collection("users").document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val displayName = document.getString("displayName") ?: displayNameFromAuth
                    val phoneNumber = document.getString("phoneNumber") ?: ""
                    val provider = document.getString("provider") ?: "email"
                    onComplete(displayName, email, phoneNumber, provider)
                } else {
                    onComplete(displayNameFromAuth, email, "", "email")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                onComplete(displayNameFromAuth, email, "", "email")
            }
    }

    private fun handleSignOut() {
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}