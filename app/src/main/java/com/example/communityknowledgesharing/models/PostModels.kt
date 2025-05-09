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
    val postId: String = "",
    val existingComments: List<String> = emptyList(),
    val likeCount: Int = 0,
    val videoUrl: String? = null
)

data class PostUIState(
    var isLiked: Boolean = false,
    var likeCount: MutableState<Int> = mutableStateOf(0),
    var comments: SnapshotStateList<String> = mutableStateListOf(),
    var newComment: MutableState<String> = mutableStateOf(""),
    var isCommentSectionVisible: Boolean = false,
    var shouldFocusComment: MutableState<Boolean> = mutableStateOf(false)
)
