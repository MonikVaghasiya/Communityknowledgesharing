// ProfileScreen.kt (fully updated)

package com.example.communityknowledgesharing.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.email?.substringBefore("@") ?: "guest"
    val context = LocalContext.current

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        profileImageUri = it
    }

    var editedName by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var editedBio by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    val skills = remember { mutableStateListOf<String>() }
    val projects = remember { mutableStateListOf<String>() }
    var newSkill by remember { mutableStateOf("") }
    var newProject by remember { mutableStateOf("") }
    var showSkillDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }

    // Load profile from Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    editedName = doc.getString("name") ?: editedName
                    editedEmail = doc.getString("email") ?: editedEmail
                    editedBio = doc.getString("bio") ?: ""
                    skills.clear()
                    skills.addAll(doc.get("skills") as? List<String> ?: emptyList())
                    projects.clear()
                    projects.addAll(doc.get("projects") as? List<String> ?: emptyList())
                    doc.getString("profileImageUrl")?.let { url ->
                        if (url.isNotBlank()) profileImageUri = Uri.parse(url)
                    }
                }
            }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("My Profile") })
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else Text("Upload")
                }
                IconButton(onClick = { imageLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editedEmail,
                    onValueChange = { editedEmail = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editedBio,
                    onValueChange = { editedBio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    if (profileImageUri != null) {
                        uploadProfileImage(profileImageUri!!, uid, onSuccess = { url ->
                            saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, url, context)
                        }, onFailure = {
                            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, null, context)
                    }
                    isEditing = false
                }, modifier = Modifier.align(Alignment.End)) {
                    Text("Save")
                }
            } else {
                Text("Name: $editedName", style = MaterialTheme.typography.titleMedium)
                Text("Email: $editedEmail")
                Text("Bio: $editedBio")
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Skills")
            skills.forEachIndexed { i, skill ->
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(skill)
                    IconButton(onClick = {
                        skills.removeAt(i)
                        saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, profileImageUri?.toString(), context)
                    }) { Icon(Icons.Default.Delete, contentDescription = null) }
                }
            }
            Button(onClick = { showSkillDialog = true }) { Text("Add Skill") }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Projects")
            projects.forEachIndexed { i, proj ->
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(proj)
                    IconButton(onClick = {
                        projects.removeAt(i)
                        saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, profileImageUri?.toString(), context)
                    }) { Icon(Icons.Default.Delete, contentDescription = null) }
                }
            }
            Button(onClick = { showProjectDialog = true }) { Text("Add Project") }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }) { Text("Logout") }
        }
    }

    if (showSkillDialog) {
        AlertDialog(
            onDismissRequest = { showSkillDialog = false },
            title = { Text("Add Skill") },
            text = {
                OutlinedTextField(value = newSkill, onValueChange = { newSkill = it }, label = { Text("Skill") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSkill.isNotBlank()) {
                        skills.add(newSkill.trim())
                        saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, profileImageUri?.toString(), context)
                        newSkill = ""
                        showSkillDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showSkillDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showProjectDialog) {
        AlertDialog(
            onDismissRequest = { showProjectDialog = false },
            title = { Text("Add Project") },
            text = {
                OutlinedTextField(value = newProject, onValueChange = { newProject = it }, label = { Text("Project") })
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newProject.isNotBlank()) {
                        projects.add(newProject.trim())
                        saveUserProfileToFirestore(uid, editedName, editedEmail, editedBio, skills, projects, profileImageUri?.toString(), context)
                        newProject = ""
                        showProjectDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showProjectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun uploadProfileImage(
    imageUri: Uri,
    uid: String,
    onSuccess: (String) -> Unit,
    onFailure: () -> Unit
) {
    val imageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
    imageRef.putFile(imageUri)
        .addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri -> onSuccess(uri.toString()) }
        }
        .addOnFailureListener { onFailure() }
}

private fun saveUserProfileToFirestore(
    uid: String,
    name: String,
    email: String,
    bio: String,
    skills: List<String>,
    projects: List<String>,
    profileImageUrl: String?,
    context: android.content.Context
) {
    val data = mapOf(
        "name" to name,
        "email" to email,
        "bio" to bio,
        "skills" to skills,
        "projects" to projects,
        "profileImageUrl" to (profileImageUrl ?: "")
    )
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .set(data)
        .addOnSuccessListener {
            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to save profile", Toast.LENGTH_SHORT).show()
        }
}
