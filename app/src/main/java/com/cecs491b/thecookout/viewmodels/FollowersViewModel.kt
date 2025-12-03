package com.cecs491b.thecookout.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cecs491b.thecookout.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class FollowersViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val usersCollection = firestore.collection("users")

    private val _uiState = MutableStateFlow(FollowersUiState())
    val uiState: StateFlow<FollowersUiState> = _uiState.asStateFlow()

    private var profileListener: ListenerRegistration? = null

    init {
        listenToCurrentUser()
    }

    // Live-updates follower counts + requests for the logged-in user
    private fun listenToCurrentUser() {
        val uid = auth.currentUser?.uid ?: return

        profileListener?.remove()
        profileListener = usersCollection.document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java) ?: return@addSnapshotListener

                _uiState.value = _uiState.value.copy(
                    followerCount = user.followers.size,
                    followingCount = user.following.size,
                    incomingRequests = user.incomingRequests,
                    outgoingRequests = user.outgoingRequests,
                    errorMessage = null
                )
            }
    }

    /** Send a follow request to targetUid */
    fun sendFollowRequest(targetUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val meRef = usersCollection.document(currentUid)
                val targetRef = usersCollection.document(targetUid)

                firestore.runBatch { batch ->
                    batch.update(
                        meRef,
                        "outgoingRequests",
                        FieldValue.arrayUnion(targetUid)
                    )
                    batch.update(
                        targetRef,
                        "incomingRequests",
                        FieldValue.arrayUnion(currentUid)
                    )
                }.await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /** Accept a follow request from fromUid */
    fun acceptFollowRequest(fromUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val meRef = usersCollection.document(currentUid)
                val fromRef = usersCollection.document(fromUid)

                firestore.runBatch { batch ->
                    // me: remove incoming request, add follower
                    batch.update(
                        meRef,
                        mapOf(
                            "incomingRequests" to FieldValue.arrayRemove(fromUid),
                            "followers" to FieldValue.arrayUnion(fromUid)
                        )
                    )

                    // follower: remove outgoing request, add following
                    batch.update(
                        fromRef,
                        mapOf(
                            "outgoingRequests" to FieldValue.arrayRemove(currentUid),
                            "following" to FieldValue.arrayUnion(currentUid)
                        )
                    )
                }.await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /** Decline a follow request without following back */
    fun declineFollowRequest(fromUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val meRef = usersCollection.document(currentUid)
                val fromRef = usersCollection.document(fromUid)

                firestore.runBatch { batch ->
                    batch.update(
                        meRef,
                        "incomingRequests",
                        FieldValue.arrayRemove(fromUid)
                    )
                    batch.update(
                        fromRef,
                        "outgoingRequests",
                        FieldValue.arrayRemove(currentUid)
                    )
                }.await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /** Cancel a follow request that *you* previously sent */
    fun cancelFollowRequest(targetUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val meRef = usersCollection.document(currentUid)
                val targetRef = usersCollection.document(targetUid)

                firestore.runBatch { batch ->
                    batch.update(
                        meRef,
                        "outgoingRequests",
                        FieldValue.arrayRemove(targetUid)
                    )
                    batch.update(
                        targetRef,
                        "incomingRequests",
                        FieldValue.arrayRemove(currentUid)
                    )
                }.await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    /** Unfollow someone you’re already following */
    fun unfollow(targetUid: String) {
        val currentUid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val meRef = usersCollection.document(currentUid)
                val targetRef = usersCollection.document(targetUid)

                firestore.runBatch { batch ->
                    batch.update(
                        meRef,
                        "following",
                        FieldValue.arrayRemove(targetUid)
                    )
                    batch.update(
                        targetRef,
                        "followers",
                        FieldValue.arrayRemove(currentUid)
                    )
                }.await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }
}

data class FollowersUiState(
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val incomingRequests: List<String> = emptyList(),
    val outgoingRequests: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
