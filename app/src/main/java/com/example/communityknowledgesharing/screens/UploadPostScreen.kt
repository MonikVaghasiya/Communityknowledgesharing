package com.example.communityknowledgesharing.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.communityknowledgesharing.models.Post
import com.example.communityknowledgesharing.models.PostUIState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPostScreen(
    navController: NavController,

    ) {
    val auth = FirebaseAuth.getInstance()
    val fullEmail = auth.currentUser?.email ?: "User"
    val username = fullEmail.substringBefore("@")

    val context = LocalContext.current

    var newPostTitle by remember { mutableStateOf("") }
    var newPostDescription by remember { mutableStateOf("") }
    var newPostImageUri by remember { mutableStateOf<Uri?>(null) }
    var videoLink by remember { mutableStateOf("")}

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        newPostImageUri = uri
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create a New Post") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = newPostTitle,
                onValueChange = { newPostTitle = it },
                label = { Text("Post Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newPostDescription,
                onValueChange = { newPostDescription = it },
                label = { Text("Post Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = videoLink,
                onValueChange = {videoLink = it},
                label = {Text("Optional Video Link (e.g Youtube")},
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Image")
            }

            newPostImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (newPostTitle.isNotBlank() && newPostDescription.isNotBlank()) {
                        if (newPostImageUri != null) {
                            uploadImageAndPost(
                                title = newPostTitle,
                                description = newPostDescription,
                                username = username,
                                imageUri = newPostImageUri!!,
                                videoUrl = if(videoLink.isNotBlank()) videoLink else null,
                                navController = navController,
                                context = context
                            )
                        } else {
                            uploadPostToFirestore(
                                title = newPostTitle,
                                description = newPostDescription,
                                username = username,
                                imageUrl = null,
                                videoUrl = if(videoLink.isNotBlank()) videoLink else null,
                                navController = navController,
                                context = context
                            )
                        }
                    } else {
                        Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post")
            }
        }
    }
}


private fun uploadImageAndPost(
    title: String,
    description: String,
    username: String,
    imageUri: Uri,
    videoUrl : String?,
    navController: NavController,
    context: android.content.Context
) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("post_images/${UUID.randomUUID()}.jpg")

    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                uploadPostToFirestore(title, description, username, downloadUri.toString(), videoUrl, navController, context)
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to upload image.", Toast.LENGTH_SHORT).show()
        }
}


private fun uploadPostToFirestore(
    title: String,
    description: String,
    username: String,
    imageUrl: String?,
    videoUrl: String?, //param for intent
    navController: NavController,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val post = hashMapOf(
        "username" to username,
        "title" to title,
        "description" to description,
        "imageUrl" to (imageUrl ?: ""),
        "videoUrl" to (videoUrl ?: ""),
<<<<<<< HEAD
        "likeCount" to 0,
        "existingComments" to emptyList<String>(),
=======
>>>>>>> 210b782c3813453ddb8473976794a5ff005e8d84
        "timestamp" to System.currentTimeMillis()
    )


    db.collection("posts")
        .add(post)
        .addOnSuccessListener {
            Toast.makeText(context, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to upload post.", Toast.LENGTH_SHORT).show()
        }
}
