package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cecs491b.thecookout.uiScreens.BottomNavBar
import com.cecs491b.thecookout.uiScreens.ProfileScreen
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.viewmodels.FollowersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.cecs491b.thecookout.models.Recipe

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val followersVm: FollowersViewModel by viewModels()

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
                                onAlertsClick = {
                                    startActivity(Intent(this, AlertsActivity::class.java))
                                },
                                onProfileClick = {
                                    // already on profile, no-op
                                }
                            )
                        }
                    ) { pad ->
                        Box(Modifier.padding(pad)) {
                            ProfileContent(followersVm)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ProfileContent(followersVm: FollowersViewModel) {
        val activity = this@ProfileActivity

        var displayName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var provider by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }

        var myRecipes by remember { mutableStateOf<List<Recipe>>(emptyList())}

        // load user profile once when this composable first appears
        LaunchedEffect(Unit) {
            activity.loadUserProfile { name, mail, phone, prov ->
                displayName = name
                email = mail
                phoneNumber = phone
                provider = prov
                isLoading = false
            }
            activity.loadMyRecipes{recipes ->
                myRecipes = recipes
            }
        }

        val followersState by followersVm.uiState.collectAsState()

        ProfileScreen(
            displayName = displayName,
            email = email,
            phoneNumber = phoneNumber,
            provider = provider,
            isLoading = isLoading,
            followerCount = followersState.followerCount,
            followingCount = followersState.followingCount,
            myRecipes = myRecipes,
            incomingRequests = followersState.incomingRequests,
            onAcceptRequest = { uid -> followersVm.acceptFollowRequest(uid) },
            onDeclineRequest = { uid -> followersVm.declineFollowRequest(uid) },
            onEditProfileClick = {
                activity.startActivity(
                    Intent(activity, EditProfileActivity::class.java)
                )
            },
            onSignOutClick = {
                activity.handleSignOut()
            },
            onSettingsClick = {
                activity.startActivity(
                    Intent(activity, SettingsActivity::class.java)
                )
            },
            onRecipeClick = { recipe ->
                val intent = Intent(activity, RecipeDetailActivity::class.java)
                intent.putExtra("recipeId", recipe.id)
                activity.startActivity(intent)
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

    private fun loadMyRecipes(onComplete: (List<Recipe>) -> Unit){
        val userId = auth.currentUser?.uid ?: return onComplete(emptyList())

        db.collection("recipes")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
               val recipes = snapshot.documents.mapNotNull {
                   doc -> doc.toObject(Recipe::class.java)
               }
                onComplete(recipes)
            }
            .addOnFailureListener {
                onComplete(emptyList())
            }
    }

    private fun handleSignOut() {
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
