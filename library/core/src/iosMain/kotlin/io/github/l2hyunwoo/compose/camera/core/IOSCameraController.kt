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

package io.github.l2hyunwoo.compose.camera.core

import androidx.compose.ui.geometry.Offset
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.cValue
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureExposureModeAutoExpose
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn
import platform.AVFoundation.AVCaptureFocusModeAutoFocus
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationBalanced
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationQuality
import platform.AVFoundation.AVCapturePhotoQualityPrioritizationSpeed
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPreset1280x720
import platform.AVFoundation.AVCaptureSessionPreset1920x1080
import platform.AVFoundation.AVCaptureSessionPreset3840x2160
import platform.AVFoundation.AVCaptureSessionPreset640x480
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.CGImageRepresentation
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.exposureMode
import platform.AVFoundation.exposurePointOfInterest
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.focusMode
import platform.AVFoundation.focusPointOfInterest
import platform.AVFoundation.hasFlash
import platform.AVFoundation.hasTorch
import platform.AVFoundation.isExposurePointOfInterestSupported
import platform.AVFoundation.isFocusPointOfInterestSupported
import platform.AVFoundation.isTorchAvailable
import platform.AVFoundation.maxAvailableVideoZoomFactor
import platform.AVFoundation.maxExposureTargetBias
import platform.AVFoundation.minAvailableVideoZoomFactor
import platform.AVFoundation.minExposureTargetBias
import platform.AVFoundation.setExposureTargetBias
import platform.AVFoundation.torchMode
import platform.AVFoundation.videoZoomFactor
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_32BGRA
import platform.Foundation.NSDate
import platform.Foundation.NSDictionary
import platform.Foundation.NSError
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.numberWithUnsignedInt
import platform.Foundation.timeIntervalSince1970
import platform.Photos.PHAssetChangeRequest
import platform.Photos.PHPhotoLibrary
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_queue_create
import platform.posix.memcpy

/**
 * iOS implementation of [CameraController] using AVFoundation.
 */
@OptIn(ExperimentalForeignApi::class)
class IOSCameraController(
  initialConfiguration: CameraConfiguration,
) : CameraController {

  private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initializing)
  private val _zoomState = MutableStateFlow(ZoomState())
  private val _exposureState = MutableStateFlow(ExposureState())

  private val iosCameraInfo = object : CameraInfo {
    override val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()
    override val zoomState: StateFlow<ZoomState> = _zoomState.asStateFlow()
    override val exposureState: StateFlow<ExposureState> = _exposureState.asStateFlow()
  }

  override val cameraInfo: CameraInfo = iosCameraInfo

  private val iosCameraControl = object : CameraControl {
    override fun setZoom(ratio: Float) {
      currentDevice?.let { device ->
        try {
          device.lockForConfiguration(null)
          val clampedRatio = ratio.toDouble().coerceIn(
            device.minAvailableVideoZoomFactor,
            device.maxAvailableVideoZoomFactor,
          )
          device.videoZoomFactor = clampedRatio
          device.unlockForConfiguration()

          _zoomState.value = _zoomState.value.copy(zoomRatio = clampedRatio.toFloat())

          val currentState = _cameraState.value
          if (currentState is CameraState.Ready) {
            _cameraState.value = currentState.copy(zoomRatio = clampedRatio.toFloat())
          }
        } catch (_: Exception) {
          // Ignore zoom errors
        }
      }
    }

    override fun focus(point: Offset) {
      currentDevice?.let { device ->
        try {
          device.lockForConfiguration(null)

          // Set focus point
          if (device.isFocusPointOfInterestSupported()) {
            device.focusPointOfInterest = cValue {
              x = point.x.toDouble()
              y = point.y.toDouble()
            }
            device.focusMode = AVCaptureFocusModeAutoFocus
          }

          // Set exposure point
          if (device.isExposurePointOfInterestSupported()) {
            device.exposurePointOfInterest = cValue {
              x = point.x.toDouble()
              y = point.y.toDouble()
            }
            device.exposureMode = AVCaptureExposureModeAutoExpose
          }

          device.unlockForConfiguration()
        } catch (_: Exception) {
          // Ignore focus errors
        }
      }
    }

    override fun setExposureCompensation(exposureValue: Float) {
      currentDevice?.let { device ->
        try {
          device.lockForConfiguration(null)
          val clampedEV = exposureValue.coerceIn(
            device.minExposureTargetBias,
            device.maxExposureTargetBias,
          )
          device.setExposureTargetBias(clampedEV) { _ ->
            // Update local state in completion handler
            _exposureState.value = _exposureState.value.copy(exposureCompensation = clampedEV)

            val currentState = _cameraState.value
            if (currentState is CameraState.Ready) {
              _cameraState.value = currentState.copy(exposureCompensation = clampedEV)
            }
          }
          device.unlockForConfiguration()
        } catch (_: Exception) {
          // Ignore exposure errors
        }
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

    override fun enableTorch(enabled: Boolean) {
      currentDevice?.let { device ->
        if (device.hasTorch && device.isTorchAvailable()) {
          try {
            device.lockForConfiguration(null)
            device.torchMode = if (enabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff
            device.unlockForConfiguration()
          } catch (_: Exception) {}
        }
      }
    }
  }

  override val cameraControl: CameraControl = iosCameraControl

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

  fun addFrameListener(listener: (CMSampleBufferRef?) -> Unit) {
    frameListeners.add(listener)
  }

  fun removeFrameListener(listener: (CMSampleBufferRef?) -> Unit) {
    frameListeners.remove(listener)
  }

  private fun setupCamera() {
    captureSession.beginConfiguration()

    // Cleanup old inputs/outputs to prevent accumulation during lens switch
    currentInput?.let { captureSession.removeInput(it) }
    photoOutput?.let { captureSession.removeOutput(it) }
    videoDataOutput?.let { captureSession.removeOutput(it) }
    videoOutput?.let { captureSession.removeOutput(it) }

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

        if (input != null && captureSession.canAddInput(input)) {
          captureSession.addInput(input)
          currentInput = input
        }

        // Add photo output
        val photo = AVCapturePhotoOutput()

        // Configure max prioritization
        if (photo.maxPhotoQualityPrioritization < AVCapturePhotoQualityPrioritizationQuality) {
          try {
            photo.maxPhotoQualityPrioritization = AVCapturePhotoQualityPrioritizationQuality
          } catch (_: Exception) {
            // Ignore if unable to set
          }
        }

        if (captureSession.canAddOutput(photo)) {
          captureSession.addOutput(photo)
          photoOutput = photo
        }

        // Video Data Output (Frame Analysis)
        val videoDataOutput = AVCaptureVideoDataOutput()
        // Using 32BGRA is generally good for ML Kit / generic processing
        // Use interpretObjCPointer to bridge CFStringRef to NSString as a dictionary key
        val settings = NSMutableDictionary().apply {
          setObject(
            NSNumber.numberWithUnsignedInt(kCVPixelFormatType_32BGRA),
            forKey = interpretObjCPointer<platform.Foundation.NSString>(kCVPixelBufferPixelFormatTypeKey!!.rawValue),
          )
        }
        videoDataOutput.videoSettings = settings as Map<Any?, *>
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

        // Initial State Update from Device
        _zoomState.value = ZoomState(
          zoomRatio = device.videoZoomFactor.toFloat(),
          minZoomRatio = device.minAvailableVideoZoomFactor.toFloat(),
          maxZoomRatio = device.maxAvailableVideoZoomFactor.toFloat(),
        )

        _exposureState.value = ExposureState(
          exposureCompensation = 0f, // Assume 0 initially or read from device if property available
          exposureCompensationRange = Pair(device.minExposureTargetBias, device.maxExposureTargetBias),
          exposureStep = 0f, // iOS doesn't explicitly expose step, continuous
        )
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

    // Set capture mode
    // Clamp to supported max prioritization
    val targetPrioritization = when (configuration.captureMode) {
      CaptureMode.QUALITY -> AVCapturePhotoQualityPrioritizationQuality
      CaptureMode.SPEED -> AVCapturePhotoQualityPrioritizationSpeed
      CaptureMode.BALANCED -> AVCapturePhotoQualityPrioritizationBalanced
    }

    val maxPrioritization = output.maxPhotoQualityPrioritization
    // Assuming enums are ordered: Speed(1) < Balanced(2) < Quality(3)
    // We can use direct comparison if they are numbers, or check specific values
    settings.photoQualityPrioritization = if (targetPrioritization > maxPrioritization) {
      maxPrioritization
    } else {
      targetPrioritization
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
    val oldPlugins = _configuration.plugins
    _configuration = config

    if (needsRebind) {
      oldPlugins.forEach { it.onDetach() }
      captureSession.stopRunning()
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
) : NSObject(),
  AVCapturePhotoCaptureDelegateProtocol {

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
        platform.UIKit.UIImage(data = data),
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
) : NSObject(),
  AVCaptureVideoDataOutputSampleBufferDelegateProtocol {

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
) : NSObject(),
  AVCaptureFileOutputRecordingDelegateProtocol {

  override fun captureOutput(
    output: AVCaptureFileOutput,
    didFinishRecordingToOutputFileAtURL: NSURL,
    fromConnections: List<*>,
    error: NSError?,
  ) {
    onFinished(error?.localizedDescription)
  }
}
