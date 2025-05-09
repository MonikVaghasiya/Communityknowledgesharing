package com.example.communityknowledgesharing.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun sendConnectionRequest(from: String, to: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("connectRequests")

    ref.whereEqualTo("from", from).whereEqualTo("to", to).get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                val request = mapOf(
                    "from" to from,
                    "to" to to,
                    "participants" to listOf(from, to),
                    "status" to "pending",
                    "timestamp" to FieldValue.serverTimestamp()
                )

                ref.add(request)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Connection request sent to @$to", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val status = result.documents.first().getString("status") ?: "pending"
                Toast.makeText(
                    context,
                    if (status == "pending") "You’ve already requested @$to" else "You are already connected with @$to",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Connection check failed", Toast.LENGTH_SHORT).show()
        }
}
