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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
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
    var text by remember { mutableStateOf(state.text.ifEmpty { "Enter text here..." }) }
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
    var showAnimation by remember { mutableStateOf(false) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            if (state.loading) {
                speechToText.stopListening()
            } else {
//                showAnimation = true
                speechToText.startListening()
            }
        }
    }



    LaunchedEffect(state) {
        text = state.text
        showAnimation = state.loading
    }
    LaunchedEffect(text) {
        translation(text, targetLanguage, "en") {
            translatedText = it
        }
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
            .background(Color(0xFFEEEEEE))
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
                        color = Color.Black,
                        fontFamily = FontFamily.Serif
                    )
                },
                Modifier
                    .padding(bottom = 2.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .fillMaxWidth(),
                actions = {
                    Button(
                        onClick = { targetCardVisible = !targetCardVisible },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = ButtonDefaults.buttonColors(Color(0xFFDFEEFF))
                    ) {
                        Text(text = targetLanguage, color = Color.Black)

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color(0xFFFFFFFF))
            )

            Card(
                modifier = Modifier
                    .height(290.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = CardDefaults.cardColors(Color(0xFFFFFFFF))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

                    TransparentTextField(modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(400.dp),
                        value = text,
                        placeholder = {Text("Enter text here...")},
                        onValueChange = {
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
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        ElevatedButton(
                            onClick = {
                                if (canSpeak) {
                                    textToSpeech.speak(text)

                                }
                            },
                            enabled = canSpeak,
                            colors = ButtonDefaults.buttonColors(Color(0xFF2196F3))
                        ) { Text("Listen", color = Color.White) }

                    }
                }
            }



            Card(
                Modifier
                    .height(280.dp)
                    .padding(horizontal = 16.dp),
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
                                        translatedText.ifEmpty { "Translated text here..." },
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Normal,
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
                        horizontalArrangement = Arrangement.End,
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
                            colors = ButtonDefaults.buttonColors(Color(0xFF2196F3))
                        ) { Text("Speak translated") }

                    }
                }

            }

            AnimatedContent(showAnimation) { show ->
                if (show)
                    MicWaveAnimation(
                        modifier = Modifier.size(100.dp),
                        isRecording = state.loading
                    )
                else Row(
                    Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .fillMaxWidth()
                        .background(Color.White),
                    horizontalArrangement = Arrangement.SpaceAround,
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

                    ElevatedButton(
                        onClick = {
                            showAnimation = true
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
                            .clip(CircleShape)
                            .size(50.dp),
                        enabled = hasPermission || !state.loading,
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFFFFF)),
                        contentPadding = PaddingValues(6.dp)
                    )
                    {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            "Speak",
                            modifier = Modifier
                                .size(40.dp),
                            tint = Color.Black
                        )
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
fun TransparentTextField(
    modifier: Modifier,
    value: String,
    placeholder: @Composable () -> Unit = {},
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
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
    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp), horizontalAlignment = Alignment.End
    ) {
        LazyColumn(
            Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF75BCFF))
                .height(500.dp)
                .width(150.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            items(languages) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Text(
                        getLanguageName(it),
                        Modifier
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .fillMaxWidth()
                            .clickable { selected(it) }, fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MicWaveAnimation(
    modifier: Modifier = Modifier,
    isRecording: Boolean
) {
    var isPlaying by remember { mutableStateOf(isRecording) }
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(100.dp)
    ) {
        if (isPlaying) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .clickable { isPlaying = false }) {
                drawCircle(
                    color = Color.Blue,
                    radius = size.minDimension / 3 * scale,
                    alpha = alpha
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Mic",
            tint = if (isRecording) Color.Red else Color.Gray,
            modifier = Modifier.size(40.dp)
        )
    }
}
