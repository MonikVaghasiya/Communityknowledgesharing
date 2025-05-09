package com.example.communityknowledgesharing.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.communityknowledgesharing.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.content.ActivityNotFoundException
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(navController: NavController, postId: String) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var post by remember { mutableStateOf<Post?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(postId) {
        db.collection("posts").document(postId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.data!!
                post = Post(
                    postId = document.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    username = data["username"] as? String ?: "",
                    imageUri = (data["imageUrl"] as? String)?.let { Uri.parse(it) },
                    existingComments = data["comments"] as? List<String> ?: emptyList(),
                    videoUrl = data["videoUrl"] as? String
                )
            }
            loading = false
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load post", Toast.LENGTH_SHORT).show()
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            post?.let { currentPost ->
                val isOwner = auth.currentUser?.email?.substringBefore("@") == currentPost.username

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = currentPost.title, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "by @${currentPost.username}", style = MaterialTheme.typography.bodySmall)

                    currentPost.imageUri?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Post Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = currentPost.description, style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${currentPost.title}\n${currentPost.description}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
                        }) {
                            Text("Share")
                        }

                        if(!currentPost.videoUrl.isNullOrBlank()){
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {

                                    currentPost.videoUrl?.let{url ->
                                        openYouTubeVideo(context, url)
                                    }
                                }
                            ){Text("Watch related video")}
                        }

                        if (isOwner) {
                            Button(
                                onClick = {
                                    db.collection("posts").document(currentPost.postId).delete()
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun openYouTubeVideo(context: Context, url: String){
    val videoId = extractYouTubeVideoId(url)

    if(videoId != null){
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))

        try{
            context.startActivity(appIntent)
        }catch (e: ActivityNotFoundException){
            context.startActivity(webIntent)
        }
    }
    else{

        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

fun extractYouTubeVideoId(url: String) : String?{
    val pattern = Pattern.compile(
        "(?:youtube\\.com.*(?:\\?|&)v=|youtu\\.be/)([a-zA-Z0-9_-]{11})",
        Pattern.CASE_INSENSITIVE
    )

    val matcher = pattern.matcher(url)
    return if(matcher.find()) matcher.group(1) else null
}
