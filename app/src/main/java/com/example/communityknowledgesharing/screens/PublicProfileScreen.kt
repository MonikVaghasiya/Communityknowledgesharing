package com.example.communityknowledgesharing.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.communityknowledgesharing.models.Post
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(username: String, navController: NavController) {
    val context = LocalContext.current
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }
    var bio by remember { mutableStateOf("") }

    LaunchedEffect(username) {
        val db = FirebaseFirestore.getInstance()

        // Fetch posts by this user
        db.collection("posts")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                posts = result.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val comments = doc.get("comments") as? List<String> ?: emptyList()

                    Post(
                        username = username,
                        title = title,
                        description = description,
                        imageUri = if (imageUrl.isNotEmpty()) android.net.Uri.parse(imageUrl) else null,
                        postId = doc.id,
                        existingComments = comments
                    )
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }

        // Fetch profile info (bio and skills)
        db.collection("users")
            .document(username)
            .get()
            .addOnSuccessListener { doc ->
                bio = doc.getString("bio") ?: "No bio provided."
                skills = doc.get("skills") as? List<String> ?: emptyList()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("@$username's Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Bio:", style = MaterialTheme.typography.titleMedium)
                Text(bio, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Skills:", style = MaterialTheme.typography.titleMedium)
                skills.forEach { skill ->
                    Text("• $skill")
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Posts:", style = MaterialTheme.typography.titleMedium)
            }

            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(post.title, style = MaterialTheme.typography.titleSmall)
                        Text(post.description, style = MaterialTheme.typography.bodySmall)
                        post.imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Connect request sent to @$username", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("Connect")
                    }
                }

            }
        }
    }
}
