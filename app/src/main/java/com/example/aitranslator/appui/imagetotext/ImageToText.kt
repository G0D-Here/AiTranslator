package com.example.aitranslator.appui.imagetotext

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.widget.LinearLayout
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun ImageToTextScreen(show: (Boolean) -> Unit = {}, text: (String) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController =
        remember { LifecycleCameraController(context) }

    var imgToText: String by remember { mutableStateOf("") }
    fun imageToText(text: String) {
        imgToText = text
        text(imgToText)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black), contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .padding(10.dp)
                .size(250.dp),
            factory = {
                PreviewView(it).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
//                    setBackgroundColor(Color.WHITE)
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }.also { previewView ->
                    startTextRecognition(
                        context,
                        cameraController,
                        lifecycleOwner,
                        previewView,
                        imageToText = ::imageToText
                    )
                }
            }
        )
        OutlinedButton(
            onClick = { show(false) },
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Text("Click", color = Color.White)
        }
    }

}

fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    imageToText: (String) -> Unit
) {
    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(imageToText)
    )
    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

class TextRecognitionAnalyzer(private val imageToText: (String) -> Unit) :
    ImageAnalysis.Analyzer {
    companion object {
        const val THROTTLE_TIME_OUT = 1_000L
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecogniser = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val mediaImage: Image =
                imageProxy.image ?: kotlin.run { imageProxy.close(); return@launch }
            val inputImage =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            suspendCoroutine { continuation ->
                textRecogniser.process(inputImage).addOnSuccessListener {
                    val detectedText: String = it.text
                    if (detectedText.isNotEmpty()) imageToText(detectedText)
                }.addOnCompleteListener {
                    continuation.resume(Unit)
                }
            }
            delay(THROTTLE_TIME_OUT)

        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }

}

fun textRecogniser(bitmap: Bitmap, textDetected: (String) -> Unit) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    scope.launch {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        suspendCoroutine { continuation ->
            textRecognizer.process(inputImage).addOnSuccessListener { text ->
                val detectedText = text.text
                if (detectedText.isNotEmpty()) textDetected(detectedText)
            }.addOnCompleteListener {
                continuation.resume(Unit)
            }
        }
    }.invokeOnCompletion { }
}
