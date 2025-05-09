package com.example.communityknowledgesharing.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
<<<<<<< HEAD
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.communityknowledgesharing.utils.sendConnectionRequest

@Composable
fun isLandscape(): Boolean {
    val config = LocalConfiguration.current
    return config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}
=======
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    val postStates = remember { mutableStateListOf<PostUIState>() }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val landscape = isLandscape()

    LaunchedEffect(Unit) {
        db.collection("posts")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
<<<<<<< HEAD
                if (error != null) return@addSnapshotListener
                val fetchedPosts = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val user = doc.getString("username") ?: ""
                    val imgUrl = doc.getString("imageUrl") ?: ""
                    val postId = doc.id
                    val video = doc.getString("videoUrl") ?: ""
                    val comments = doc.get("existingComments") as? List<String> ?: emptyList()
                    val likeCount = doc.getLong("likeCount")?.toInt() ?: 0

                    Post(user, title, desc,
                        imageUri = if (imgUrl.isNotEmpty()) Uri.parse(imgUrl) else null,
                        postId = postId,
                        existingComments = comments,
                        likeCount = likeCount,
                        videoUrl = video
=======
                if (error != null) {
                    Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val fetchedPosts = snapshot?.documents?.mapNotNull { document ->
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description") ?: ""
                    val user = document.getString("username") ?: ""
                    val imageUrl = document.getString("imageUrl") ?: ""
                    val comments = document.get("existingComments") as? List<String> ?: emptyList()
                    Post(
                        username = user,
                        title = title,
                        description = description,
                        imageUri = if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null,
                        postId = document.id,
                        existingComments = comments
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
                    )
                } ?: emptyList()

                posts = fetchedPosts.reversed()
                postStates.clear()
                postStates.addAll(posts.map {
                    PostUIState(
                        likeCount = mutableStateOf(it.likeCount),
                        comments = mutableStateListOf(*it.existingComments.toTypedArray()),
                        newComment = mutableStateOf(""),
                        isCommentSectionVisible = false,
                        shouldFocusComment = mutableStateOf(false)
                    )
                })
            }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("upload") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Post")
            }
        }
    ) { inner ->
        if (landscape) {
            Row(
                modifier = Modifier
                    .padding(inner)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search posts, skills, users...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(2f)
                ) {
                    itemsIndexed(posts.zip(postStates)) { index, (post, state) ->
                        if (searchQuery.text.isBlank() ||
                            post.title.contains(searchQuery.text, true) ||
                            post.description.contains(searchQuery.text, true) ||
                            post.username.contains(searchQuery.text, true)) {
                            PostItem(post, state, listState, index, navController)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(inner)
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
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    itemsIndexed(posts.zip(postStates)) { index, (post, state) ->
                        if (searchQuery.text.isBlank() ||
                            post.title.contains(searchQuery.text, true) ||
                            post.description.contains(searchQuery.text, true) ||
                            post.username.contains(searchQuery.text, true)) {
                            PostItem(post, state, listState, index, navController)
                        }
                    }
                }
            }
        }
    }
}


<<<<<<< HEAD


@Composable
fun PostItem(post: Post, state: PostUIState, listState: LazyListState, index: Int, navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
=======
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(enabled = !state.isCommentSectionVisible) {
                navController.navigate("postDetail/${post.postId}")
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                post.username,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable {
                    navController.navigate("publicProfile/${post.username}")
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                post.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.clickable {
                    if (!state.isCommentSectionVisible)
                        navController.navigate("postDetail/${post.postId}")
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(post.description)

            post.imageUri?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            post.videoUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }) {
                    Text("▶ Watch Video", color = Color.White)
                }
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
fun PostActionsRow(post: Post, state: PostUIState, listState: LazyListState, index: Int, focusManager: FocusManager) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

<<<<<<< HEAD
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconToggleButton(
                checked = state.isLiked,
                onCheckedChange = {
                    state.isLiked = it
                    val change = if (it) 1 else -1
                    state.likeCount.value += change
                    db.collection("posts").document(post.postId)
                        .update("likeCount", FieldValue.increment(change.toLong()))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (state.isLiked) Color.Red else Color.Gray
                )
            }
            Text("${state.likeCount.value} likes")
=======
    val shouldRequestFocus = remember {mutableStateOf(false)}

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionIconButton(if (state.isLiked) "❤️ ${state.likeCount.value}" else "🤍 ${state.likeCount.value}") {
            state.isLiked = !state.isLiked
            if (state.isLiked) state.likeCount.value++ else state.likeCount.value = (state.likeCount.value - 1).coerceAtLeast(0)
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
        }

        TextButton(onClick = {
            state.isCommentSectionVisible = !state.isCommentSectionVisible
<<<<<<< HEAD
            if (state.isCommentSectionVisible) {
                coroutineScope.launch {
                    listState.animateScrollToItem(index)
                    state.shouldFocusComment.value = true
                }
            } else {
                state.shouldFocusComment.value = false
            }
        }) {
            Text(if (state.isCommentSectionVisible) "💬 Hide" else "💬 Comment")
=======

            if(state.isCommentSectionVisible){
                coroutineScope.launch{
                    listState.animateScrollToItem(index)
                    state.shouldFocusComment.value = true
                }

            }
            else{
                state.shouldFocusComment.value = false
            }
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
        }

        TextButton(onClick = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.description}")
            }
            context.startActivity(Intent.createChooser(intent, "Share Post"))
        }) {
            Text("🔗 Share")
        }

        TextButton(onClick = {
            val currentUser =
                FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: "unknown"
            sendConnectionRequest(currentUser, post.username, context)

        }) {
            Text("🤝 Connect")
        }
    }
}

@Composable
fun CommentSection(post: Post, state: PostUIState) {
    val db = FirebaseFirestore.getInstance()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val isFirstAppearance = remember{mutableStateOf(true)}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {

        if (state.comments.isNotEmpty()) {
            state.comments.forEach { comment ->
                Text("• $comment", style = MaterialTheme.typography.bodySmall)
            }
            Divider(
                color = Color.Gray,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Text("No comments yet", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }

<<<<<<< HEAD

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
=======
        key(state.isCommentSectionVisible) {

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
        }
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
<<<<<<< HEAD
                val comment = state.newComment.value.trim()
                if (comment.isNotBlank()) {
                    db.collection("posts").document(post.postId)
                        .update("existingComments", FieldValue.arrayUnion(comment))
                        .addOnSuccessListener {
                            // Update local UI
                            state.comments.add(comment)
                            state.newComment.value = ""
                            focusManager.clearFocus()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                        }
=======
                val trimmed = state.newComment.value.trim()
                if (trimmed.isNotEmpty()) {
                    /*state.comments.add(trimmed)
                    db.collection("posts").document(post.postId)
                        .update("comments", FieldValue.arrayUnion(trimmed))
                    state.newComment.value = ""*/
                    //use function to add comments//
                    addComment(
                        post = post,
                        comment = trimmed,
                        onSuccess ={
                            state.comments.add(trimmed)
                            state.newComment.value = ""
                            focusManager.clearFocus()
                        },
                        onError = { e ->
                            //handles error if comment can't be made//
                            Log.e("CommentSection", "Error adding comment", e)
                        }
                    )
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
                }
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB))
        ) {
            Text("Post Comment")
        }
    }
<<<<<<< HEAD


    LaunchedEffect(state.shouldFocusComment.value) {
        if (state.shouldFocusComment.value) {
            delay(200)
            runCatching { focusRequester.requestFocus() }
            state.shouldFocusComment.value = false
        }
    }
}
fun sendConnectionRequest(from: String, to: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val connectRef = db.collection("connectRequests")

    connectRef
        .whereEqualTo("from", from)
        .whereEqualTo("to", to)
        .get()
        .addOnSuccessListener { existing ->
            if (existing.isEmpty) {
                val request = mapOf(
                    "from" to from,
                    "to" to to,
                    "participants" to listOf(from, to),
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                connectRef.add(request)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Request sent to @$to", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Request already exists", Toast.LENGTH_SHORT).show()
            }
        }
=======
    //Handles focusing in attempt to fix bug where comments open the search before the comment textbox//
    LaunchedEffect(state.isCommentSectionVisible) {
        if (state.isCommentSectionVisible) {
            //multiple focus attempts with delays to help with the bug//
            delay(100)
            try { focusRequester.requestFocus() } catch (e: Exception) { }
            delay(200)
            try { focusRequester.requestFocus() } catch (e: Exception) { }
            delay(300)
            try { focusRequester.requestFocus() } catch (e: Exception) { }

            //resets the flag after attempts//
            state.shouldFocusComment.value = false

        }
    }
}
fun addComment(post: Post, comment: String, onSuccess: () -> Unit, onError: (Exception) -> Unit){
    val db = FirebaseFirestore.getInstance()
    val updatedComments = post.existingComments.toMutableList().apply{add(comment)}

    db.collection("posts").document(post.postId)
        .update("existingComments", updatedComments)
        .addOnSuccessListener{onSuccess()}
        .addOnFailureListener{ e -> onError(e)}
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
}
