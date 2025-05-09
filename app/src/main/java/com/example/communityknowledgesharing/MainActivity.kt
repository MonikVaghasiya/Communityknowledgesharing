package com.example.communityknowledgesharing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.communityknowledgesharing.navigation.AppNavigation

import com.example.communityknowledgesharing.ui.theme.CommunityKnowledgeSharingTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommunityKnowledgeSharingTheme {
                AppNavigation()
                FirebaseFirestore.setLoggingEnabled(true)

            }
        }
    }
}
