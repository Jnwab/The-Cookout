package com.cecs491b.thecookout.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val provider: String = "", // the only options should be email or google or phone
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val incomingRequests: List<String> = emptyList(),
    val outgoingRequests: List<String> = emptyList()
)