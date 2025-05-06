package com.example.communityknowledgesharing.models

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// 🐾 Data class for a Post
data class Post(
    val username: String,
    val title: String,
    val description: String,
    val imageUri: Uri? = null,
    val postId: String = "",                    // ✅ Firestore Document ID
    val existingComments: List<String> = emptyList()  // ✅ Existing Comments
)

// 🐾 UI State class for Post
data class PostUIState(
    var isLiked: Boolean = false,
    var likeCount: MutableState<Int> = mutableStateOf(0),
    var isCommentSectionVisible: Boolean = false,
    var comments: SnapshotStateList<String> = mutableStateListOf(),
    var newComment: MutableState<String> = mutableStateOf("")
)
