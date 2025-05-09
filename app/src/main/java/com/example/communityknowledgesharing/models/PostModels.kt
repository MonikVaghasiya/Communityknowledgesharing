package com.example.communityknowledgesharing.models

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList


data class Post(
    val username: String,
    val title: String,
    val description: String,
    val imageUri: Uri? = null,
<<<<<<< HEAD
    val postId: String = "",
    val existingComments: List<String> = emptyList(),
    val likeCount: Int = 0,
    val videoUrl: String? = null
=======
    val postId: String = "",                    // ✅ Firestore Document ID
    val existingComments: List<String> = emptyList(),  // ✅ Existing Comments
    val videoUrl: String? = null //for the Youtube intent
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
)

data class PostUIState(
    var isLiked: Boolean = false,
    var likeCount: MutableState<Int> = mutableStateOf(0),
    var comments: SnapshotStateList<String> = mutableStateListOf(),
    var newComment: MutableState<String> = mutableStateOf(""),
<<<<<<< HEAD
    var isCommentSectionVisible: Boolean = false,
=======
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
    var shouldFocusComment: MutableState<Boolean> = mutableStateOf(false)
)
