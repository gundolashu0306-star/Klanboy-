package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.core.logging.AppLogger
import com.example.data.firebase.FirebaseAuthRepository
import com.example.data.firebase.FirebaseStorageRepository
import com.example.data.firebase.FirestoreService
import com.example.navigation.CampusPlugAppRoot
import com.example.ui.theme.CampusPlugTheme

class MainActivity : ComponentActivity() {

    private val authRepository by lazy { FirebaseAuthRepository() }
    private val firestoreService by lazy { FirestoreService(applicationContext) }
    private val storageRepository by lazy { FirebaseStorageRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i("MainActivity onCreate - Initializing Campus Plug SA UI")
        
        enableEdgeToEdge()

        setContent {
            CampusPlugTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusPlugAppRoot(
                        authRepository = authRepository,
                        firestoreService = firestoreService,
                        storageRepository = storageRepository
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLogger.d("MainActivity onResume")
    }
}
