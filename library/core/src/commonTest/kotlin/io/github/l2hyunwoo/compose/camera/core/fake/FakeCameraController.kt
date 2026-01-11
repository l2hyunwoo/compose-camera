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
package io.github.l2hyunwoo.compose.camera.core.fake

import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraControl
import io.github.l2hyunwoo.compose.camera.core.CameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraException
import io.github.l2hyunwoo.compose.camera.core.CameraInfo
import io.github.l2hyunwoo.compose.camera.core.CameraLens
import io.github.l2hyunwoo.compose.camera.core.CameraState
import io.github.l2hyunwoo.compose.camera.core.CaptureConfig
import io.github.l2hyunwoo.compose.camera.core.FlashMode
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.PreviewSurfaceProvider
import io.github.l2hyunwoo.compose.camera.core.RecordingConfig
import io.github.l2hyunwoo.compose.camera.core.VideoCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.VideoRecording
import io.github.l2hyunwoo.compose.camera.core.VideoRecordingResult

/**
 * Fake implementation of [CameraController] for testing.
 * Supports the full extensibility API for comprehensive testing.
 */
class FakeCameraController(
  initialConfiguration: CameraConfiguration = CameraConfiguration(),
) : CameraController {

  // State
  enum class ControllerState {
    NOT_INITIALIZED,
    INITIALIZING,
    READY,
    STOPPED,
    RELEASED,
  }

  private var _state: ControllerState = ControllerState.NOT_INITIALIZED
  val controllerState: ControllerState get() = _state

  // Dependencies
  private val _cameraInfo = FakeCameraInfo()
  override val cameraInfo: CameraInfo get() = _cameraInfo

  private val _cameraControl = FakeCameraControl()
  override val cameraControl: CameraControl get() = _cameraControl

  private var _configuration: CameraConfiguration = initialConfiguration
  override val configuration: CameraConfiguration get() = _configuration

  // Use cases
  private var _imageCaptureUseCase: ImageCaptureUseCase = FakeImageCaptureUseCase()
  override var imageCaptureUseCase: ImageCaptureUseCase
    get() = _imageCaptureUseCase
    set(value) {
      _imageCaptureUseCase = value
    }

  private var _videoCaptureUseCase: VideoCaptureUseCase = FakeVideoCaptureUseCase()
  override var videoCaptureUseCase: VideoCaptureUseCase
    get() = _videoCaptureUseCase
    set(value) {
      _videoCaptureUseCase = value
    }

  // Extensions
  private val _extensions = mutableMapOf<String, CameraControlExtension>()

  // Preview surface provider
  private var _previewSurfaceProvider: PreviewSurfaceProvider? = null
  val previewSurfaceProvider: PreviewSurfaceProvider? get() = _previewSurfaceProvider

  // Call tracking
  var initializeCallCount = 0
    private set
  var startCallCount = 0
    private set
  var stopCallCount = 0
    private set
  var releaseCallCount = 0
    private set
  var takePictureCallCount = 0
    private set
  var startRecordingCallCount = 0
    private set

  // Configuration for test behavior
  var initializeException: Throwable? = null
  var takePictureResult: ImageCaptureResult = ImageCaptureResult.Success(
    byteArray = ByteArray(0),
    width = 1920,
    height = 1080,
  )
  var startRecordingResult: VideoRecording = FakeVideoRecording(
    VideoRecordingResult.Success(uri = "file:///test/video.mp4", durationMs = 5000L),
  )

  /**
   * Initialize the camera controller.
   * Transitions from NOT_INITIALIZED to READY state.
   */
  suspend fun initialize() {
    checkNotReleased()
    initializeCallCount++

    initializeException?.let { throw it }

    _state = ControllerState.INITIALIZING
    _cameraInfo.setCameraState(CameraState.Initializing)

    // Simulate initialization
    _state = ControllerState.READY
    _cameraInfo.transitionToReady(
      lens = _configuration.lens,
      flashMode = _configuration.flashMode,
    )

    // Notify extensions
    _extensions.values.forEach { it.onCameraReady() }
  }

  /**
   * Start the camera preview.
   */
  suspend fun start() {
    checkNotReleased()
    checkInitialized()
    startCallCount++
    _state = ControllerState.READY
    _cameraInfo.transitionToReady(
      lens = _configuration.lens,
      flashMode = _configuration.flashMode,
    )
  }

  /**
   * Stop the camera preview (but keep resources allocated).
   */
  suspend fun stop() {
    checkNotReleased()
    stopCallCount++
    _state = ControllerState.STOPPED
  }

  override suspend fun takePicture(): ImageCaptureResult {
    checkNotReleased()
    checkInitialized()
    takePictureCallCount++

    val config = CaptureConfig(
      flashMode = _configuration.flashMode,
      resolution = _configuration.photoResolution,
      outputFormat = _configuration.imageFormat,
    )

    return _imageCaptureUseCase.capture(this, config)
  }

  override suspend fun startRecording(): VideoRecording {
    checkNotReleased()
    checkInitialized()
    startRecordingCallCount++

    val config = RecordingConfig(
      quality = _configuration.videoQuality,
      enableAudio = true,
    )

    return _videoCaptureUseCase.startRecording(this, config)
  }

  override fun updateConfiguration(config: CameraConfiguration) {
    checkNotReleased()
    _configuration = config
  }

  override fun setLens(lens: CameraLens) {
    checkNotReleased()
    _configuration = _configuration.copy(lens = lens)
    if (_state == ControllerState.READY) {
      _cameraInfo.transitionToReady(lens = lens, flashMode = _configuration.flashMode)
    }
  }

  override fun release() {
    if (_state == ControllerState.RELEASED) return

    releaseCallCount++

    // Notify extensions before release
    _extensions.values.forEach { it.onCameraReleased() }

    // Detach all extensions
    _extensions.values.toList().forEach { ext ->
      ext.onDetach()
    }
    _extensions.clear()

    // Notify preview surface provider
    _previewSurfaceProvider?.onSurfaceDestroyed()
    _previewSurfaceProvider = null

    _state = ControllerState.RELEASED
    _cameraInfo.setCameraState(
      CameraState.Error(CameraException.Unknown("Camera released")),
    )
  }

  // Extension management

  /**
   * Get a registered extension by ID.
   */
  @Suppress("UNCHECKED_CAST")
  override fun <T : CameraControlExtension> getExtension(id: String): T? = _extensions[id] as? T

  /**
   * Register a camera control extension.
   * @throws IllegalArgumentException if an extension with the same ID is already registered
   */
  override fun registerExtension(extension: CameraControlExtension) {
    checkNotReleased()

    if (_extensions.containsKey(extension.id)) {
      throw IllegalArgumentException("Extension with id '${extension.id}' is already registered")
    }

    _extensions[extension.id] = extension
    extension.onAttach(this)

    // If camera is already ready, notify extension
    if (_state == ControllerState.READY) {
      extension.onCameraReady()
    }
  }

  /**
   * Unregister a camera control extension.
   */
  override fun unregisterExtension(id: String) {
    checkNotReleased()

    val extension = _extensions.remove(id)
    extension?.onDetach()
  }

  // Preview surface provider

  /**
   * Set the preview surface provider.
   */
  override fun setPreviewSurfaceProvider(provider: PreviewSurfaceProvider?) {
    checkNotReleased()

    // Destroy previous provider
    _previewSurfaceProvider?.onSurfaceDestroyed()

    _previewSurfaceProvider = provider

    // Simulate surface available if camera is ready
    if (_state == ControllerState.READY && provider != null) {
      provider.onSurfaceAvailable("fake-surface")
    }
  }

  // Helper methods

  private fun checkNotReleased() {
    if (_state == ControllerState.RELEASED) {
      throw IllegalStateException("CameraController has been released")
    }
  }

  private fun checkInitialized() {
    if (_state == ControllerState.NOT_INITIALIZED) {
      throw IllegalStateException("CameraController not initialized. Call initialize() first.")
    }
  }

  /**
   * Get the fake CameraInfo for test configuration.
   */
  fun getFakeCameraInfo(): FakeCameraInfo = _cameraInfo

  /**
   * Get the fake CameraControl for test configuration.
   */
  fun getFakeCameraControl(): FakeCameraControl = _cameraControl

  /**
   * Reset all state for testing.
   */
  fun reset() {
    _state = ControllerState.NOT_INITIALIZED
    _configuration = CameraConfiguration()
    _cameraInfo.setCameraState(CameraState.Initializing)
    _cameraControl.reset()
    _extensions.clear()
    _previewSurfaceProvider = null
    initializeCallCount = 0
    startCallCount = 0
    stopCallCount = 0
    releaseCallCount = 0
    takePictureCallCount = 0
    startRecordingCallCount = 0
    initializeException = null
  }
}
