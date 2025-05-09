package com.example.communityknowledgesharing.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.communityknowledgesharing.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(username: String, navController: NavController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: "unknown"
    val db = FirebaseFirestore.getInstance()

    var isConnected by remember { mutableStateOf(false) }
    var materials by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }
    var bio by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(username) {

        db.collection("connectRequests")
            .whereEqualTo("status", "accepted")
            .whereArrayContains("participants", currentUser)
            .get()
            .addOnSuccessListener { docs ->
                isConnected = docs.any {
                    it.getString("from") == username || it.getString("to") == username
                }
            }


        db.collection("users").document(username).get()
            .addOnSuccessListener { doc ->
                bio = doc.getString("bio") ?: "No bio provided."
                skills = doc.get("skills") as? List<String> ?: emptyList()


                if (isConnected) {
                    val rawMaterials = doc.get("materials") as? List<Map<String, String>> ?: emptyList()
                    materials = rawMaterials.mapNotNull {
                        val title = it["title"]
                        val url = it["url"]
                        if (!title.isNullOrBlank() && !url.isNullOrBlank()) title to url else null
                    }
                }
            }


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
                        imageUri = if (imageUrl.isNotEmpty()) Uri.parse(imageUrl) else null,
                        postId = doc.id,
                        existingComments = comments
                    )
                }
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
                Text(bio)
                Spacer(modifier = Modifier.height(12.dp))

                Text("Skills:", style = MaterialTheme.typography.titleMedium)
                skills.forEach { Text("• $it") }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Materials:", style = MaterialTheme.typography.titleMedium)
                if (isConnected) {
                    if (materials.isEmpty()) {
                        Text("No materials shared.")
                    } else {
                        materials.forEach { (title, link) ->
                            Text(
                                text = "• $title",
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                } else {
                    Text("Connect with @$username to view materials.")
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
                if (currentUser != username) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                sendConnectionRequest(currentUser, username, context)
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
}


