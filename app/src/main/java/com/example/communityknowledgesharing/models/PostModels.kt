package com.example.communityknowledgesharing.models

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// 🐾 Data class for a Post (from Firestore)
data class Post(
    val username: String,
    val title: String,
    val description: String,
    val imageUri: Uri? = null,
    val postId: String = "",                           // ✅ Firestore Document ID
    val existingComments: List<String> = emptyList(),  // ✅ Existing Comments
    val likeCount: Int = 0,                            // ✅ New: Initial like count from Firestore
    val videoUrl: String? = null                       // 🎥 Optional: YouTube URL if available
)

data class PostUIState(
    var isLiked: Boolean = false,
    var likeCount: MutableState<Int> = mutableStateOf(0),
    var comments: SnapshotStateList<String> = mutableStateListOf(),
    var newComment: MutableState<String> = mutableStateOf(""),  // ✅ Required!
    var isCommentSectionVisible: Boolean = false,
    var shouldFocusComment: MutableState<Boolean> = mutableStateOf(false)  // ✅ Required!
)
