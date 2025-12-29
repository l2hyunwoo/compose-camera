/*
 * Copyright (C) 2025 l2hyunwoo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.l2hyunwoo.compose.camera.core

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Android implementation of [CameraController] using CameraX.
 */
class AndroidCameraController(
  private val context: Context,
  private val lifecycleOwner: LifecycleOwner,
  initialConfiguration: CameraConfiguration,
) : CameraController {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val executor = Executors.newSingleThreadExecutor()

  private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initializing)
  override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

  private val _zoomRatioFlow = MutableStateFlow(1.0f)
  override val zoomRatioFlow: StateFlow<Float> = _zoomRatioFlow.asStateFlow()

  override val minZoomRatio: Float
    get() = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1.0f

  override val maxZoomRatio: Float
    get() = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f

  private var _configuration = initialConfiguration
  override val configuration: CameraConfiguration get() = _configuration

  // CameraX components
  private var cameraProvider: ProcessCameraProvider? = null
  private var camera: Camera? = null
  private var preview: Preview? = null
  private var imageCapture: ImageCapture? = null
  private var videoCapture: VideoCapture<Recorder>? = null
  private var activeRecording: Recording? = null

  // Surface request for Compose viewfinder
  private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
  internal val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

  /**
   * Initialize the camera with the given configuration
   */
  internal suspend fun initialize() {
    try {
      cameraProvider = ProcessCameraProvider.awaitInstance(context)
      bindCamera()
    } catch (e: Exception) {
      _cameraState.value = CameraState.Error(
        CameraException.InitializationFailed(e),
      )
    }
  }

  private fun bindCamera() {
    val provider = cameraProvider ?: return

    try {
      // Unbind all use cases before rebinding
      provider.unbindAll()

      // Build preview use case
      preview = Preview.Builder().build().apply {
        setSurfaceProvider { request ->
          _surfaceRequest.value = request
        }
      }

      // Build image capture use case
      imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .setFlashMode(configuration.flashMode.toCameraXFlashMode())
        .build()

      // Build video capture use case with fallback strategy
      val qualitySelector = QualitySelector.fromOrderedList(
        listOf(
          configuration.videoQuality.toCameraXQuality(),
          Quality.HD,
          Quality.SD,
        ),
        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD),
      )
      val recorder = Recorder.Builder()
        .setQualitySelector(qualitySelector)
        .build()
      videoCapture = VideoCapture.withOutput(recorder)

      // Select camera
      val cameraSelector = when (configuration.lens) {
        CameraLens.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
        CameraLens.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
      }

      // ImageAnalysis
      val analysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
      imageAnalysis = analysis
      updateImageAnalysis()

      // Attach plugins first to register any analyzers
      configuration.plugins.forEach { plugin ->
        plugin.onAttach(this)
      }

      // Bind use cases to lifecycle
      camera = provider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture,
        videoCapture,
        analysis,
      )

      // Observe zoom state changes
      camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { zoomState ->
        _zoomRatioFlow.value = zoomState.zoomRatio
        val currentState = _cameraState.value
        if (currentState is CameraState.Ready) {
          _cameraState.value = currentState.copy(zoomRatio = zoomState.zoomRatio)
        }
      }

      // Update state to ready
      _cameraState.value = CameraState.Ready(
        currentLens = configuration.lens,
        flashMode = configuration.flashMode,
        isRecording = false,
        zoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1.0f,
      )
    } catch (e: Exception) {
      _cameraState.value = CameraState.Error(
        CameraException.InitializationFailed(e),
      )
    }
  }

  override suspend fun takePicture(): ImageCaptureResult {
    val capture = imageCapture ?: return ImageCaptureResult.Error(
      CameraException.CaptureFailed(IllegalStateException("ImageCapture not initialized")),
    )

    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ComposeCamera")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
      context.contentResolver,
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      contentValues,
    ).build()

    return suspendCancellableCoroutine { continuation ->
      capture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
          override fun onImageSaved(output: ImageCapture.OutputFileResults) {
            val uri = output.savedUri
            // Get image info from the saved file
            val result = ImageCaptureResult.Success(
              // Image saved to file, not in memory
              byteArray = ByteArray(0),
              width = 0,
              height = 0,
              rotation = 0,
              filePath = uri?.toString(),
            )
            continuation.resume(result)
          }

          override fun onError(exception: ImageCaptureException) {
            continuation.resume(
              ImageCaptureResult.Error(CameraException.CaptureFailed(exception)),
            )
          }
        },
      )
    }
  }

  override suspend fun startRecording(): VideoRecording {
    val capture = videoCapture ?: throw CameraException.RecordingFailed(
      IllegalStateException("VideoCapture not initialized"),
    )

    // Create output options for MediaStore
    val name = "VID_${System.currentTimeMillis()}.mp4"
    val contentValues = ContentValues().apply {
      put(MediaStore.Video.Media.DISPLAY_NAME, name)
      put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
      put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ComposeCamera")
    }

    val outputOptions = MediaStoreOutputOptions.Builder(
      context.contentResolver,
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
    ).setContentValues(contentValues).build()

    // Prepare recording
    val pendingRecording = capture.output.prepareRecording(context, outputOptions)

    // Enable audio if permission granted
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
      == PackageManager.PERMISSION_GRANTED
    ) {
      pendingRecording.withAudioEnabled()
    }

    // Start recording
    val recording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { event ->
      when (event) {
        is VideoRecordEvent.Start -> {
          val currentState = _cameraState.value
          if (currentState is CameraState.Ready) {
            _cameraState.value = currentState.copy(isRecording = true)
          }
        }

        is VideoRecordEvent.Finalize -> {
          val currentState = _cameraState.value
          if (currentState is CameraState.Ready) {
            _cameraState.value = currentState.copy(isRecording = false)
          }
        }
      }
    }

    activeRecording = recording

    return AndroidVideoRecording(
      recording = recording,
      outputUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString() + "/" + name,
    )
  }

  override fun updateConfiguration(config: CameraConfiguration) {
    val needsRebind = config.lens != _configuration.lens
    _configuration = config

    // Update flash mode without rebinding
    imageCapture?.flashMode = config.flashMode.toCameraXFlashMode()

    if (needsRebind) {
      bindCamera()
    }
  }

  override fun setLens(lens: CameraLens) {
    if (lens != configuration.lens) {
      updateConfiguration(configuration.copy(lens = lens))
    }
  }

  override fun setFlashMode(mode: FlashMode) {
    _configuration = configuration.copy(flashMode = mode)
    imageCapture?.flashMode = mode.toCameraXFlashMode()

    val currentState = _cameraState.value
    if (currentState is CameraState.Ready) {
      _cameraState.value = currentState.copy(flashMode = mode)
    }
  }

  override fun setZoom(ratio: Float) {
    camera?.cameraControl?.setZoomRatio(ratio)
  }

  override fun focus(point: Offset) {
    val cameraControl = camera?.cameraControl ?: return
    val meteringPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
      .createPoint(point.x, point.y)

    val action = FocusMeteringAction.Builder(meteringPoint)
      .setAutoCancelDuration(5, java.util.concurrent.TimeUnit.SECONDS)
      .build()

    cameraControl.startFocusAndMetering(action)
  }

  private val analyzers = mutableListOf<ImageAnalysis.Analyzer>()
  private var imageAnalysis: ImageAnalysis? = null

  fun addAnalyzer(analyzer: ImageAnalysis.Analyzer) {
    analyzers.add(analyzer)
    updateImageAnalysis()
  }

  fun removeAnalyzer(analyzer: ImageAnalysis.Analyzer) {
    analyzers.remove(analyzer)
    updateImageAnalysis()
  }

  private fun updateImageAnalysis() {
    imageAnalysis?.setAnalyzer(executor) { imageProxy ->
      if (analyzers.isEmpty()) {
        imageProxy.close()
        return@setAnalyzer
      }

      // Each analyzer is responsible for closing the imageProxy after processing.
      // Note: Currently supports single analyzer effectively; multiple analyzers
      // require the last one to close the proxy.
      analyzers.forEach { it.analyze(imageProxy) }
    }
  }

  override fun release() {
    // Detach plugins
    configuration.plugins.forEach { plugin ->
      plugin.onDetach()
    }

    activeRecording?.stop()
    cameraProvider?.unbindAll()
    executor.shutdown()
  }
}

/**
 * Android implementation of [VideoRecording]
 */
internal class AndroidVideoRecording(
  private val recording: Recording,
  private val outputUri: String,
) : VideoRecording {

  private var _isRecording = true
  override val isRecording: Boolean get() = _isRecording

  private var startTimeMs = System.currentTimeMillis()

  override suspend fun stop(): VideoRecordingResult = suspendCancellableCoroutine { continuation ->
    recording.stop()
    _isRecording = false

    val durationMs = System.currentTimeMillis() - startTimeMs
    continuation.resume(
      VideoRecordingResult.Success(
        uri = outputUri,
        durationMs = durationMs,
      ),
    )
  }

  override fun pause() {
    recording.pause()
  }

  override fun resume() {
    recording.resume()
  }
}

// Extension functions for enum conversions
private fun FlashMode.toCameraXFlashMode(): Int = when (this) {
  FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
  FlashMode.ON -> ImageCapture.FLASH_MODE_ON
  FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
  FlashMode.TORCH -> ImageCapture.FLASH_MODE_ON
}

private fun VideoQuality.toCameraXQuality(): Quality = when (this) {
  VideoQuality.SD -> Quality.SD
  VideoQuality.HD -> Quality.HD
  VideoQuality.FHD -> Quality.FHD
  VideoQuality.UHD -> Quality.UHD
}
