package com.example.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

fun Bitmap.compressToJpegBytes(quality: Int = 75, maxDimension: Int = 1024): ByteArray {
    var scaledBitmap = this
    val width = this.width
    val height = this.height
    if (width > maxDimension || height > maxDimension) {
        val ratio = width.toFloat() / height.toFloat()
        val (newWidth, newHeight) = if (ratio > 1f) {
            maxDimension to (maxDimension / ratio).toInt()
        } else {
            (maxDimension * ratio).toInt() to maxDimension
        }
        scaledBitmap = Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    }

    val stream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

/**
 * Fast scene difference detector to prevent sending duplicate static frames,
 * drastically reducing API calls and permanently preventing free-tier 429 quota exhaustion.
 */
fun Bitmap.computeSceneDifference(other: Bitmap?): Float {
    if (other == null) return 1.0f
    return try {
        val sampleSize = 16
        val b1 = Bitmap.createScaledBitmap(this, sampleSize, sampleSize, false)
        val b2 = Bitmap.createScaledBitmap(other, sampleSize, sampleSize, false)

        var totalDiff = 0L
        for (x in 0 until sampleSize) {
            for (y in 0 until sampleSize) {
                val p1 = b1.getPixel(x, y)
                val p2 = b2.getPixel(x, y)
                val rDiff = kotlin.math.abs(android.graphics.Color.red(p1) - android.graphics.Color.red(p2))
                val gDiff = kotlin.math.abs(android.graphics.Color.green(p1) - android.graphics.Color.green(p2))
                val bDiff = kotlin.math.abs(android.graphics.Color.blue(p1) - android.graphics.Color.blue(p2))
                totalDiff += (rDiff + gDiff + bDiff)
            }
        }
        val maxDiff = sampleSize * sampleSize * 255L * 3L
        (totalDiff.toFloat() / maxDiff.toFloat())
    } catch (_: Exception) {
        1.0f
    }
}

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    isTorchOn: Boolean,
    isFrontCamera: Boolean,
    onPreviewViewReady: (PreviewView) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(previewView) {
        onPreviewViewReady(previewView)
    }

    LaunchedEffect(isFrontCamera) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(isTorchOn, camera) {
        if (!isFrontCamera) {
            camera?.cameraControl?.enableTorch(isTorchOn)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
