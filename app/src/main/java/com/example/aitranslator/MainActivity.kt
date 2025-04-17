package com.example.aitranslator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.aitranslator.appui.speechtotext.MainScreen
import com.example.aitranslator.ui.theme.AiTranslatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiTranslatorTheme {
                val context = LocalContext.current
                var hasPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(
                            context,

                            Manifest.permission.RECORD_AUDIO

                        ) == PackageManager.PERMISSION_GRANTED
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted ->
                    hasPermission = granted
                }

                LaunchedEffect(Unit) {
                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                if (hasPermission) {
                    MainScreen()
                } else {
                    // Optional: Show a permission rationale UI
                    PermissionDeniedScreen {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Microphone permission is required for speech recognition")
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        }
    }
}