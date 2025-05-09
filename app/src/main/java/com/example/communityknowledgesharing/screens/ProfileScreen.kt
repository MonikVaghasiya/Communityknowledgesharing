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

data class MaterialEntry(val title: String, val url: String)

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
    val materials = remember { mutableStateListOf<MaterialEntry>() }

    var newSkill by remember { mutableStateOf("") }
    var newProject by remember { mutableStateOf("") }
    var newMaterialTitle by remember { mutableStateOf("") }
    var newMaterialUrl by remember { mutableStateOf("") }

    var showSkillDialog by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var showMaterialDialog by remember { mutableStateOf(false) }


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
                    val rawMaterials = doc.get("materials") as? List<Map<String, String>> ?: emptyList()
                    materials.clear()
                    materials.addAll(rawMaterials.mapNotNull { it["title"]?.let { t -> MaterialEntry(t, it["url"] ?: "") } })
                    doc.getString("profileImageUrl")?.let { url ->
                        if (url.isNotBlank()) profileImageUri = Uri.parse(url)
                    }
                }
            }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("My Profile") }) }) { padding ->
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
                OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editedEmail, onValueChange = { editedEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editedBio, onValueChange = { editedBio = it }, label = { Text("Bio") }, modifier = Modifier.fillMaxWidth())

                Button(onClick = {
                    if (profileImageUri != null) {
                        uploadProfileImage(profileImageUri!!, uid, onSuccess = { url ->
                            saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, url, context)
                        }, onFailure = {
                            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                        })
                    } else {
                        saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, null, context)
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

            SectionList("Skills", skills, onAdd = { showSkillDialog = true }, onDelete = {
                skills.remove(it)
                saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
            })

            SectionList("Projects", projects, onAdd = { showProjectDialog = true }, onDelete = {
                projects.remove(it)
                saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
            })

            SectionList("My Materials", materials.map { "${it.title}: ${it.url}" }, onAdd = { showMaterialDialog = true }, onDelete = { item ->
                materials.removeIf { "${it.title}: ${it.url}" == item }
                saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
            })

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Text("Logout")
            }
        }
    }

    if (showSkillDialog) {
        InputDialog("Add Skill", newSkill, onValueChange = { newSkill = it }, onAdd = {
            if (newSkill.isNotBlank()) {
                skills.add(newSkill)
                newSkill = ""
                saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
            }
            showSkillDialog = false
        }, onDismiss = { showSkillDialog = false })
    }

    if (showProjectDialog) {
        InputDialog("Add Project", newProject, onValueChange = { newProject = it }, onAdd = {
            if (newProject.isNotBlank()) {
                projects.add(newProject)
                newProject = ""
                saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
            }
            showProjectDialog = false
        }, onDismiss = { showProjectDialog = false })
    }

    if (showMaterialDialog) {
        MaterialInputDialog(
            title = newMaterialTitle,
            url = newMaterialUrl,
            onTitleChange = { newMaterialTitle = it },
            onUrlChange = { newMaterialUrl = it },
            onAdd = {
                if (newMaterialTitle.isNotBlank() && newMaterialUrl.isNotBlank()) {
                    materials.add(MaterialEntry(newMaterialTitle, newMaterialUrl))
                    newMaterialTitle = ""
                    newMaterialUrl = ""
                    saveUserProfile(uid, editedName, editedEmail, editedBio, skills, projects, materials, profileImageUri?.toString(), context)
                }
                showMaterialDialog = false
            },
            onDismiss = { showMaterialDialog = false }
        )
    }
}

@Composable
fun SectionList(
    title: String,
    items: List<String>,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit
) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    items.forEach { item ->
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(item)
            IconButton(onClick = { onDelete(item) }) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
    Button(onClick = onAdd) { Text("Add ${title.removePrefix("My ").dropLast(1)}") }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun InputDialog(title: String, value: String, onValueChange: (String) -> Unit, onAdd: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(title) })
        },
        confirmButton = { TextButton(onClick = onAdd) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun MaterialInputDialog(
    title: String,
    url: String,
    onTitleChange: (String) -> Unit,
    onUrlChange: (String) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Material") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("Title") })
                OutlinedTextField(value = url, onValueChange = onUrlChange, label = { Text("Link URL") })
            }
        },
        confirmButton = { TextButton(onClick = onAdd) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun uploadProfileImage(imageUri: Uri, uid: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
    val imageRef = FirebaseStorage.getInstance().reference.child("profile_images/$uid.jpg")
    imageRef.putFile(imageUri)
        .addOnSuccessListener { imageRef.downloadUrl.addOnSuccessListener { uri -> onSuccess(uri.toString()) } }
        .addOnFailureListener { onFailure() }
}

private fun saveUserProfile(
    uid: String,
    name: String,
    email: String,
    bio: String,
    skills: List<String>,
    projects: List<String>,
    materials: List<MaterialEntry>,
    profileImageUrl: String?,
    context: android.content.Context
) {
    val data = mapOf(
        "name" to name,
        "email" to email,
        "bio" to bio,
        "skills" to skills,
        "projects" to projects,
        "materials" to materials.map { mapOf("title" to it.title, "url" to it.url) },
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
