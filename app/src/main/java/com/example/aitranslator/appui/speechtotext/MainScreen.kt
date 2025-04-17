package com.example.aitranslator.appui.speechtotext

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.aitranslator.appui.imagetotext.ImageToTextScreen
import com.example.aitranslator.appui.imagetotext.textRecogniser
import com.example.aitranslator.services.SpeechToText
import com.example.aitranslator.services.TextToSpeech
import com.example.aitranslator.services.getAllLanguages
import com.example.aitranslator.services.getLanguageName
import com.example.aitranslator.services.languageIdentifier
import com.example.aitranslator.services.translation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val speechToText = remember { SpeechToText(application) }
    val allLanguages = getAllLanguages()
    val state by speechToText.state.collectAsState()
    var text by remember { mutableStateOf(state.text) }
    var canSpeak by remember { mutableStateOf(false) }


    var translatedText by remember { mutableStateOf("") }
    var isSpeaking by remember { mutableStateOf(false) }

    var sourceLanguage by remember { mutableStateOf("en") }
    var targetLanguage by remember { mutableStateOf("hi") }

    var targetCardVisible by remember { mutableStateOf(false) }
    var sourceCardVisible by remember { mutableStateOf(false) }

    var enteredLangCode by remember { mutableStateOf("") }
    var showCamera by remember { mutableStateOf(false) }


    val textToSpeech = TextToSpeech(context) { isSuccess ->
        canSpeak = isSuccess
    }

    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            if (state.loading) speechToText.stopListening() else speechToText.startListening()
        }
    }


    LaunchedEffect(state) {
        text = state.text
    }
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { it ->
            val source = ImageDecoder.createSource(context.contentResolver, it)
            bitmap = ImageDecoder.decodeBitmap(source)
            bitmap?.let {
                textRecogniser(it) { detected ->
                    text = detected
                }
            }
        }
    }

    Box(
        Modifier
            .background(Color(0xFFFFFFFF))
            .fillMaxSize()
            .padding(), contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {
                    Text(
                        "Translator",
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                },
                Modifier
                    .padding(bottom = 2.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .fillMaxWidth(),
                actions = {
                    Button(
                        onClick = { targetCardVisible = !targetCardVisible },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFFFFF))
                    ) {
                        Text(text = targetLanguage, color = Color.Black)

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFF9C27B0))
            )

            ElevatedCard(
                modifier = Modifier
                    .height(290.dp)
                    .padding(16.dp),
                elevation = CardDefaults.elevatedCardElevation(3.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFFFFF))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

                    TransparentTextField(modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(400.dp), value = text, onValueChange = {
                        text = it
                        languageIdentifier(text) { out ->
                            enteredLangCode = out
                            Log.d("IDENTIFY", "this is the code: $out")
                        }
                    }
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElevatedButton(
                            onClick = {
                                textToSpeech.stop()
                                translatedText = ""
                                if (hasPermission) {
                                    if (state.loading) {
                                        text = ""
                                        speechToText.stopListening()
                                    } else speechToText.startListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(40.dp),
                            enabled = hasPermission || !state.loading,
                            colors = ButtonDefaults.buttonColors(Color(0xFF000000)),
                            elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                            contentPadding = PaddingValues(6.dp)
                        )
                        {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                "Speak",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(0.dp),
                                tint = Color.White
                            )
                        }

                        Text(text = "Det. lang $enteredLangCode")
                        ElevatedButton(
                            onClick = {
                                if (canSpeak) {
                                    textToSpeech.speak(text)

                                }
                            },
                            enabled = canSpeak,
                            colors = ButtonDefaults.buttonColors(Color(0xFF9C27B0))
                        ) { Text("Listen", color = Color.White) }

                    }
                }
            }


            ElevatedCard(
                Modifier
                    .height(290.dp)
                    .padding(start = 16.dp, end = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xFF9C27B0)),
                elevation = CardDefaults.elevatedCardElevation(3.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFFFFF))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    LazyColumn(
                        Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                            .heightIn(400.dp)
                    ) {
                        item {
                            AnimatedContent(targetState = state) { currentState ->
                                when {
                                    !hasPermission -> Text("Microphone permission required")
                                    currentState.loading -> Text("Speak in English")
                                    true -> Text(
                                        translatedText.ifEmpty { "Click to translate..." },
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )

                                    else -> Text("Error: ${currentState.error}")
                                }
                            }
                        }
                    }

                    Row(
                        Modifier
                            .padding(bottom = 10.dp, start = 6.dp, end = 6.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElevatedButton(

                            onClick = {
                                isSpeaking = !isSpeaking
                                if (canSpeak && !isSpeaking) {
                                    textToSpeech.speak(translatedText)
                                } else textToSpeech.stop()
                            },
                            enabled = canSpeak,
                            colors = ButtonDefaults.buttonColors(Color(0xFF000000))
                        ) { Text("Speak translated") }

                        ElevatedButton(
                            onClick = {
                                translation(
                                    text = text,
                                    targetLanguage = targetLanguage,
                                    sourceLanguage = "en"
                                ) {
                                    translatedText = it
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFF9C27B0)),
                        ) {
                            Text("Translate")
                        }
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        showCamera = !showCamera
                    },
                    Modifier
                        .padding(10.dp)
                        .size(100.dp, 40.dp)
                        .clip(CircleShape)
                ) {
                    Text("Camera")
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch("image/*")
                    }, Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .padding(10.dp)
                        .size(95.dp, 40.dp)
                ) {
                    Text("Gallery")
                }
            }
        }
        if (targetCardVisible)
            AllLanguagesCard(allLanguages) {
                targetLanguage = it
                targetCardVisible = false
            }
        if (sourceCardVisible)
            AllLanguagesCard(allLanguages) {
                sourceLanguage = it
                sourceCardVisible = false

            }
        AnimatedContent(showCamera) { show ->
            if (show) {
                ImageToTextScreen(show = { showCamera = it }) {
                    text = it
                }
            }
        }
    }

}

@Composable
fun TransparentTextField(modifier: Modifier, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            cursorColor = Color.Black
        )
    )
}


@Composable
fun AllLanguagesCard(languages: List<String>, selected: (String) -> Unit = {}) {
    ElevatedCard(
        Modifier
            .padding(30.dp),
    ) {
        LazyColumn(
            Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .height(400.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            items(languages) {
                Text(getLanguageName(it), Modifier.clickable { selected(it) })
            }
        }
    }
}