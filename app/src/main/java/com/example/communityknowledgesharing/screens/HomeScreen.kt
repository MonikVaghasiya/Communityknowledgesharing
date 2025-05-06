package com.example.communityknowledgesharing.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.communityknowledgesharing.models.Post
import com.example.communityknowledgesharing.models.PostUIState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val fullEmail = auth.currentUser?.email ?: "User"
    val username = fullEmail.substringBefore("@")
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val lazyListState = rememberLazyListState()

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val postStates = remember { mutableStateListOf<PostUIState>() }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("posts")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val fetchedPosts = snapshot?.documents?.mapNotNull { document ->
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val user = document.getString("username") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val comments = document.get("comments") as? List<String> ?: emptyList()
                    Post(
                        username = user,
                        title = title,
                        description = description,
                        imageUri = if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null,
                        postId = document.id,
                        existingComments = comments
                    )
                } ?: emptyList()

                posts = fetchedPosts.reversed()

                if (posts.size != postStates.size) {
                    postStates.clear()
                    fetchedPosts.forEach {
                        postStates.add(PostUIState(comments = mutableStateListOf(*it.existingComments.toTypedArray())))
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Community Feed") },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                TabRow(selectedTabIndex = 0) {
                    Tab(
                        selected = true,
                        onClick = { },
                        text = { Text("Home") }
                    )
                    Tab(
                        selected = false,
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("home") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        text = { Text("Profile") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("upload") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search posts, skills, users...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(posts.zip(postStates)) { index, (post, state) ->
                    if (post.title.contains(searchQuery.text, ignoreCase = true) ||
                        post.description.contains(searchQuery.text, ignoreCase = true) ||
                        post.username.contains(searchQuery.text, ignoreCase = true)
                    ) {
                        PostItem(post, state, lazyListState, index, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    state: PostUIState,
    listState: LazyListState,
    index: Int,
    navController: NavController
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                navController.navigate("postDetail/${post.postId}")
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                post.username,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable {
                    if (post.username.isNotEmpty()) {
                        navController.navigate("publicProfile/${post.username}")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(post.description, style = MaterialTheme.typography.bodyMedium)

            post.imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PostActionsRow(post, state, listState, index, focusManager)

            AnimatedVisibility(state.isCommentSectionVisible) {
                CommentSection(post, state)
            }
        }
    }
}

@Composable
fun PostActionsRow(
    post: Post,
    state: PostUIState,
    listState: LazyListState,
    index: Int,
    focusManager: FocusManager
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionIconButton(if (state.isLiked) "❤️ ${state.likeCount.value}" else "🤍 ${state.likeCount.value}") {
            state.isLiked = !state.isLiked
            if (state.isLiked) state.likeCount.value++ else state.likeCount.value = (state.likeCount.value - 1).coerceAtLeast(0)
        }
        ActionIconButton(if (state.isCommentSectionVisible) "💬 Hide" else "💬 Comment") {
            focusManager.clearFocus()
            state.isCommentSectionVisible = !state.isCommentSectionVisible
        }
        ActionIconButton("🔗 Share") {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.description}")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
        }
        ActionIconButton("🤝 Connect") {
            Toast.makeText(context, "Connect request sent to ${post.username}", Toast.LENGTH_SHORT).show()
        }
    }

    if (state.isCommentSectionVisible) {
        LaunchedEffect(index) {
            listState.animateScrollToItem(index)
        }
    }
}

@Composable
fun ActionIconButton(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text)
    }
}

@Composable
fun CommentSection(post: Post, state: PostUIState) {
    val db = FirebaseFirestore.getInstance()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {
        if (state.comments.isNotEmpty()) {
            state.comments.forEach { comment ->
                Text(text = "• $comment", style = MaterialTheme.typography.bodySmall)
            }
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
        }

        OutlinedTextField(
            value = state.newComment.value,
            onValueChange = { state.newComment.value = it },
            label = { Text("Write a comment...") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val trimmed = state.newComment.value.trim()
                if (trimmed.isNotEmpty()) {
                    state.comments.add(trimmed)
                    db.collection("posts").document(post.postId)
                        .update("comments", FieldValue.arrayUnion(trimmed))
                    state.newComment.value = ""
                }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB))
        ) {
            Text("Post Comment")
        }
    }

    LaunchedEffect(state.isCommentSectionVisible) {
        if (state.isCommentSectionVisible) {
            focusRequester.requestFocus()
        }
    }
}
