package com.cecs491b.thecookout.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cecs491b.thecookout.BuildConfig
import com.cecs491b.thecookout.uiScreens.EditProfileScreen
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EditProfileScreen(
                        onSaveClick = { displayName, phoneNumber ->
                            handleProfileUpdate(displayName, phoneNumber)
                        },
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }

    private fun handleProfileUpdate(displayName: String, phoneNumber: String) {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        if (displayName.isBlank()) {
            Toast.makeText(this, "Display name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update Firestore database
                    updateDatabase(user.uid, displayName, phoneNumber)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to update profile: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun updateDatabase(uid: String, displayName: String, phoneNumber: String) {
        val updates = mapOf(
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to save changes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}