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
@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package io.github.l2hyunwoo.compose.camera

import androidx.compose.ui.geometry.Offset
import kotlinx.cinterop.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.*
import platform.CoreMedia.*
import platform.CoreVideo.*
import platform.Foundation.*
import platform.Photos.*
import platform.UIKit.UIImage
import platform.darwin.*
import platform.posix.memcpy

/**
 * iOS implementation of [CameraController] using AVFoundation.
 */
@OptIn(ExperimentalForeignApi::class)
internal class IOSCameraController(
  initialConfiguration: CameraConfiguration,
) : CameraController {

  private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initializing)
  override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

  private var _configuration = initialConfiguration
  override val configuration: CameraConfiguration get() = _configuration

  // AVFoundation components
  internal val captureSession = AVCaptureSession()
  private var currentDevice: AVCaptureDevice? = null
  private var photoOutput: AVCapturePhotoOutput? = null
  private var videoOutput: AVCaptureMovieFileOutput? = null
  private var currentInput: AVCaptureDeviceInput? = null

  // Photo capture completion
  private var photoCaptureCompletion: CompletableDeferred<ImageCaptureResult>? = null
  private var photoDelegate: PhotoCaptureDelegate? = null

  // Video recording
  private var activeVideoRecording: IOSVideoRecording? = null

  /**
   * Initialize the camera with the given configuration
   */
  internal fun initialize() {
    try {
      setupCamera()
    } catch (e: Exception) {
      _cameraState.value = CameraState.Error(
        CameraException.InitializationFailed(e),
      )
    }
  }

  private val videoDataOutputQueue = dispatch_queue_create("video_data_queue", null)
  private var videoDataOutput: AVCaptureVideoDataOutput? = null
  private val frameListeners = mutableListOf<(CMSampleBufferRef?) -> Unit>()

  internal fun addFrameListener(listener: (CMSampleBufferRef?) -> Unit) {
    frameListeners.add(listener)
  }

  internal fun removeFrameListener(listener: (CMSampleBufferRef?) -> Unit) {
    frameListeners.remove(listener)
  }

  private fun setupCamera() {
    captureSession.beginConfiguration()

    // Set session preset
    captureSession.sessionPreset = when (configuration.videoQuality) {
      VideoQuality.SD -> AVCaptureSessionPreset640x480
      VideoQuality.HD -> AVCaptureSessionPreset1280x720
      VideoQuality.FHD -> AVCaptureSessionPreset1920x1080
      VideoQuality.UHD -> AVCaptureSessionPreset3840x2160
    }

    // Get camera device
    val position = when (configuration.lens) {
      CameraLens.FRONT -> AVCaptureDevicePositionFront
      CameraLens.BACK -> AVCaptureDevicePositionBack
    }

    currentDevice = AVCaptureDevice.defaultDeviceWithDeviceType(
      deviceType = AVCaptureDeviceTypeBuiltInWideAngleCamera,
      mediaType = AVMediaTypeVideo,
      position = position,
    )

    currentDevice?.let { device ->
      try {
        val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
          as? AVCaptureDeviceInput

        if (input != null && captureSession.canAddInput(input)) {
          captureSession.addInput(input)
          currentInput = input
        }

        // Add photo output
        val photo = AVCapturePhotoOutput()
        if (captureSession.canAddOutput(photo)) {
          captureSession.addOutput(photo)
          photoOutput = photo
        }

        // Video Data Output (Frame Analysis)
        val videoDataOutput = AVCaptureVideoDataOutput()
        // Using 32BGRA is generally good for ML Kit / generic processing
        val settings = mapOf<Any?, Any>(kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32BGRA)
        videoDataOutput.videoSettings = settings
        videoDataOutput.alwaysDiscardsLateVideoFrames = true

        val delegate = VideoDataDelegate { buffer ->
          if (frameListeners.isNotEmpty()) {
            frameListeners.forEach { it(buffer) }
          }
        }
        videoDataOutput.setSampleBufferDelegate(delegate, videoDataOutputQueue)

        if (captureSession.canAddOutput(videoDataOutput)) {
          captureSession.addOutput(videoDataOutput)
          this.videoDataOutput = videoDataOutput
        }

        // Add video output
        val video = AVCaptureMovieFileOutput()
        if (captureSession.canAddOutput(video)) {
          captureSession.addOutput(video)
          videoOutput = video
        }
      } catch (e: Exception) {
        _cameraState.value = CameraState.Error(
          CameraException.InitializationFailed(e),
        )
        return
      }
    }

    captureSession.commitConfiguration()

    // Start session on background thread
    val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0.toULong())
    dispatch_async(queue) {
      captureSession.startRunning()
    }

    // Update state
    _cameraState.value = CameraState.Ready(
      currentLens = configuration.lens,
      flashMode = configuration.flashMode,
      isRecording = false,
      zoomRatio = 1.0f,
    )

    // Attach plugins
    configuration.plugins.forEach { plugin ->
      plugin.onAttach(this)
    }
  }

  override suspend fun takePicture(): ImageCaptureResult {
    val output = photoOutput ?: return ImageCaptureResult.Error(
      CameraException.CaptureFailed(IllegalStateException("PhotoOutput not initialized")),
    )

    val completion = CompletableDeferred<ImageCaptureResult>()
    photoCaptureCompletion = completion

    // Create photo settings
    val settings = AVCapturePhotoSettings()

    // Set flash mode
    if (currentDevice?.hasFlash == true) {
      settings.flashMode = when (configuration.flashMode) {
        FlashMode.OFF -> AVCaptureFlashModeOff
        FlashMode.ON -> AVCaptureFlashModeOn
        FlashMode.AUTO -> AVCaptureFlashModeAuto
        FlashMode.TORCH -> AVCaptureFlashModeOff
      }
    }

    // Create delegate
    val delegate = PhotoCaptureDelegate { result ->
      completion.complete(result)
    }
    photoDelegate = delegate

    // Capture photo
    output.capturePhotoWithSettings(settings, delegate)

    return completion.await()
  }

  override suspend fun startRecording(): VideoRecording {
    val output = videoOutput ?: throw CameraException.RecordingFailed(
      IllegalStateException("VideoOutput not initialized"),
    )

    // Create temp file URL
    val tempDir = NSTemporaryDirectory()
    val fileName = "VID_${NSDate().timeIntervalSince1970.toLong()}.mov"
    val fileURL = NSURL.fileURLWithPath(tempDir + fileName)

    val recording = IOSVideoRecording(
      output = output,
      fileURL = fileURL,
      onStateChange = { isRecording ->
        val currentState = _cameraState.value
        if (currentState is CameraState.Ready) {
          _cameraState.value = currentState.copy(isRecording = isRecording)
        }
      },
    )

    recording.start()
    activeVideoRecording = recording

    return recording
  }

  override fun updateConfiguration(config: CameraConfiguration) {
    val needsRebind = config.lens != _configuration.lens
    _configuration = config

    if (needsRebind) {
      captureSession.stopRunning()

      // Remove existing inputs
      currentInput?.let { input ->
        captureSession.removeInput(input)
      }

      setupCamera()
    }

    // Update flash/torch
    updateFlashMode(config.flashMode)
  }

  override fun setLens(lens: CameraLens) {
    if (lens != configuration.lens) {
      updateConfiguration(configuration.copy(lens = lens))
    }
  }

  override fun setFlashMode(mode: FlashMode) {
    _configuration = configuration.copy(flashMode = mode)
    updateFlashMode(mode)

    val currentState = _cameraState.value
    if (currentState is CameraState.Ready) {
      _cameraState.value = currentState.copy(flashMode = mode)
    }
  }

  private fun updateFlashMode(mode: FlashMode) {
    currentDevice?.let { device ->
      if (device.hasTorch && device.isTorchAvailable()) {
        try {
          device.lockForConfiguration(null)
          device.torchMode = when (mode) {
            FlashMode.TORCH -> AVCaptureTorchModeOn
            else -> AVCaptureTorchModeOff
          }
          device.unlockForConfiguration()
        } catch (_: Exception) {
          // Ignore errors for torch mode
        }
      }
    }
  }

  override fun setZoom(ratio: Float) {
    currentDevice?.let { device ->
      try {
        device.lockForConfiguration(null)
        device.videoZoomFactor = ratio.toDouble().coerceIn(
          device.minAvailableVideoZoomFactor,
          device.maxAvailableVideoZoomFactor,
        )
        device.unlockForConfiguration()

        val currentState = _cameraState.value
        if (currentState is CameraState.Ready) {
          _cameraState.value = currentState.copy(zoomRatio = ratio)
        }
      } catch (_: Exception) {
        // Ignore zoom errors
      }
    }
  }

  override fun focus(point: Offset) {
    currentDevice?.let { device ->
      if (device.isFocusPointOfInterestSupported()) {
        try {
          device.lockForConfiguration(null)
          device.focusMode = AVCaptureFocusModeAutoFocus
          device.unlockForConfiguration()
        } catch (_: Exception) {
          // Ignore focus errors
        }
      }
    }
  }

  override fun release() {
    // Detach plugins
    configuration.plugins.forEach { plugin ->
      plugin.onDetach()
    }

    activeVideoRecording?.stopImmediate()
    captureSession.stopRunning()
  }
}

/**
 * Delegate for photo capture callbacks
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class PhotoCaptureDelegate(
  private val onResult: (ImageCaptureResult) -> Unit,
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {

  override fun captureOutput(
    output: AVCapturePhotoOutput,
    didFinishProcessingPhoto: AVCapturePhoto,
    error: NSError?,
  ) {
    if (error != null) {
      onResult(
        ImageCaptureResult.Error(
          CameraException.CaptureFailed(Exception(error.localizedDescription)),
        ),
      )
      return
    }

    val data = didFinishProcessingPhoto.fileDataRepresentation()
    if (data == null) {
      onResult(
        ImageCaptureResult.Error(
          CameraException.CaptureFailed(Exception("Failed to get photo data")),
        ),
      )
      return
    }

    // Convert NSData to ByteArray
    val length = data.length.toInt()
    val bytes = ByteArray(length)

    bytes.usePinned { pinned ->
      memcpy(pinned.addressOf(0), data.bytes, data.length)
    }

    // Get dimensions from photo
    val cgImage = didFinishProcessingPhoto.CGImageRepresentation()
    val width = cgImage?.let { platform.CoreGraphics.CGImageGetWidth(it).toInt() } ?: 0
    val height = cgImage?.let { platform.CoreGraphics.CGImageGetHeight(it).toInt() } ?: 0

    // Save to Photo Library
    // Save to Photo Library
    PHPhotoLibrary.sharedPhotoLibrary().performChanges({
      PHAssetChangeRequest.creationRequestForAssetFromImage(
        platform.UIKit.UIImage(data = data!!),
      )
    }, completionHandler = { success, error ->
      if (success) {
        // Return success with bytes (file path extraction is complex on iOS without identifier)
        // Ideally we should return the PHAsset identifier but for now we return the bytes
        // and let the MediaLoader fetch the latest asset
        onResult(
          ImageCaptureResult.Success(
            byteArray = bytes,
            width = width,
            height = height,
            rotation = 0,
            // TODO: Pass localIdentifier if API supports it
          ),
        )
      } else {
        onResult(
          ImageCaptureResult.Error(
            CameraException.CaptureFailed(Exception(error?.localizedDescription ?: "Save failed")),
          ),
        )
      }
    })
  }
}

/**
 * iOS implementation of VideoRecording
 */
@OptIn(ExperimentalForeignApi::class)
internal class IOSVideoRecording(
  private val output: AVCaptureMovieFileOutput,
  private val fileURL: NSURL,
  private val onStateChange: (Boolean) -> Unit,
) : VideoRecording {

  private var _isRecording = false
  override val isRecording: Boolean get() = _isRecording

  private var startTimeMs = 0L
  private var recordingCompletion: CompletableDeferred<VideoRecordingResult>? = null
  private var recordingDelegate: VideoRecordingDelegate? = null

  internal fun start() {
    val delegate = VideoRecordingDelegate { error ->
      _isRecording = false
      onStateChange(false)

      if (error != null) {
        recordingCompletion?.complete(
          VideoRecordingResult.Error(CameraException.RecordingFailed(Exception(error))),
        )
        return@VideoRecordingDelegate
      }

      // Save to Photo Library
      PHPhotoLibrary.sharedPhotoLibrary().performChanges({
        PHAssetChangeRequest.creationRequestForAssetFromVideoAtFileURL(fileURL)
      }, completionHandler = { success, saveError ->
        val durationMs = (NSDate().timeIntervalSince1970 * 1000).toLong() - startTimeMs

        if (success) {
          val result = VideoRecordingResult.Success(
            // Return temp URL or fetch asset URL
            uri = fileURL.absoluteString ?: "",
            durationMs = durationMs,
          )
          recordingCompletion?.complete(result)
        } else {
          val result = VideoRecordingResult.Error(
            CameraException.RecordingFailed(
              Exception(saveError?.localizedDescription ?: "Save failed"),
            ),
          )
          recordingCompletion?.complete(result)
        }

        // Cleanup temp file
        // NSFileManager.defaultManager.removeItemAtURL(fileURL, null)
        // Don't delete immediately if we return the temp URL
      })
    }
    recordingDelegate = delegate

    output.startRecordingToOutputFileURL(fileURL, delegate)
    _isRecording = true
    startTimeMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
    onStateChange(true)
  }

  override suspend fun stop(): VideoRecordingResult {
    val completion = CompletableDeferred<VideoRecordingResult>()
    recordingCompletion = completion
    output.stopRecording()
    return completion.await()
  }

  internal fun stopImmediate() {
    if (_isRecording) {
      output.stopRecording()
      _isRecording = false
    }
  }

  override fun pause() {
    output.pauseRecording()
  }

  override fun resume() {
    output.resumeRecording()
  }
}

/**
 * Delegate for video data output (frame analysis)
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class VideoDataDelegate(
  private val onFrame: (CMSampleBufferRef?) -> Unit,
) : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

  override fun captureOutput(
    output: AVCaptureOutput,
    didOutputSampleBuffer: CMSampleBufferRef?,
    fromConnection: AVCaptureConnection,
  ) {
    onFrame(didOutputSampleBuffer)
  }
}

/**
 * Delegate for video recording callbacks
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class VideoRecordingDelegate(
  private val onFinished: (String?) -> Unit,
) : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {

  override fun captureOutput(
    output: AVCaptureFileOutput,
    didFinishRecordingToOutputFileAtURL: NSURL,
    fromConnections: List<*>,
    error: NSError?,
  ) {
    onFinished(error?.localizedDescription)
  }
}
