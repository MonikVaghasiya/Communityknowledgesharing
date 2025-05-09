package com.example.communityknowledgesharing.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api :: class)
@Composable
fun ConnectionsScreen(navController : NavController){
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") ?: ""
    var receivedRequests by remember { mutableStateOf<List<String>>(emptyList()) }
    var sentRequests by remember { mutableStateOf<List<String>>(emptyList()) }
    var acceptedConnections by remember { mutableStateOf<List<String>>(emptyList()) }

    fun loadRequests() {
        val db = FirebaseFirestore.getInstance()

        // Real-time listener for pending received requests
        db.collection("connectRequests")
            .whereEqualTo("to", currentUser)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { docs, _ ->
                receivedRequests = docs?.documents?.mapNotNull { it.getString("from") } ?: emptyList()
            }

        // Real-time listener for sent requests
        // Real-time listener for sent pending requests
        db.collection("connectRequests")
            .whereEqualTo("from", currentUser)
            .whereEqualTo("status", "pending") // ✅ Only pending ones
            .addSnapshotListener { docs, _ ->
                sentRequests = docs?.documents?.mapNotNull { it.getString("to") } ?: emptyList()
            }


        // Real-time listener for accepted connections
        db.collection("connectRequests")
            .whereArrayContains("participants", currentUser)
            .whereEqualTo("status", "accepted")
            .addSnapshotListener { docs, _ ->
                acceptedConnections = docs?.documents?.mapNotNull {
                    val from = it.getString("from")
                    val to = it.getString("to")
                    if (from == currentUser) to else from
                } ?: emptyList()
            }
    }

    // Initial load
    LaunchedEffect(Unit) { loadRequests() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Connections") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Received Requests", style = MaterialTheme.typography.titleMedium)
            if (receivedRequests.isEmpty()) {
                Text("No pending requests")
            } else {
                receivedRequests.forEach { name ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name)
                        Row {
                            TextButton(onClick = {
                                acceptRequest(name, currentUser, context) { loadRequests() }
                            }) {
                                Text("Accept")
                            }
                            TextButton(onClick = {
                                rejectRequest(name, currentUser, context) { loadRequests() }
                            }) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Sent Requests", style = MaterialTheme.typography.titleMedium)
            sentRequests.forEach { to ->
                Text("\u2022 $to")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Accepted Connections", style = MaterialTheme.typography.titleMedium)
            if (acceptedConnections.isEmpty()) {
                Text("No connections yet")
            } else {
                acceptedConnections.forEach {
                    Text("\u2022 $it")
                }
            }
        }
    }
}
fun acceptRequest(from: String, to: String, context: Context, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("connectRequests")
        .whereEqualTo("from", from)
        .whereEqualTo("to", to)
        .get()
        .addOnSuccessListener { result ->
            for (doc in result.documents) {
                db.collection("connectRequests").document(doc.id)
                    .update(
                        "status", "accepted",
                        "participants", listOf(from, to)
                    )
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
                    }
            }
            Toast.makeText(context, "Accepted @$from", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to find request", Toast.LENGTH_SHORT).show()
        }
}
fun rejectRequest(from: String, to: String, context: Context, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("connectRequests")
        .whereEqualTo("from", from)
        .whereEqualTo("to", to)
        .get()
        .addOnSuccessListener { result ->
            for (doc in result.documents) {
                db.collection("connectRequests").document(doc.id)
                    .delete()
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to reject request", Toast.LENGTH_SHORT).show()
                    }
            }
            Toast.makeText(context, "Rejected @$from", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to find request", Toast.LENGTH_SHORT).show()
        }
}