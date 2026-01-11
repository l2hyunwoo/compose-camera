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

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines

/**
 * Controller for camera operations.
 * Provides access to camera information and controls, as well as methods for capturing media.
 */
interface CameraController {

  /**
   * Access to camera configuration and capabilities.
   */
  val cameraInfo: CameraInfo

  /**
   * Access to camera controls (zoom, focus, exposure, etc.).
   */
  val cameraControl: CameraControl

  /**
   * Current camera configuration
   */
  val configuration: CameraConfiguration

  /**
   * Custom image capture use case.
   * Set to override the default image capture behavior.
   */
  var imageCaptureUseCase: ImageCaptureUseCase

  /**
   * Custom video capture use case.
   * Set to override the default video capture behavior.
   */
  var videoCaptureUseCase: VideoCaptureUseCase

  /**
   * Capture a photo
   * @return The result of the image capture operation
   */
  @NativeCoroutines
  suspend fun takePicture(): ImageCaptureResult

  /**
   * Start video recording
   * @return A [VideoRecording] handle to control the recording
   */
  @NativeCoroutines
  suspend fun startRecording(): VideoRecording

  /**
   * Update the camera configuration.
   * This will apply the new configuration to the camera.
   *
   * @param config The new configuration to apply
   */
  fun updateConfiguration(config: CameraConfiguration)

  /**
   * Set the camera lens (front/back)
   * This delegates to updateConfiguration internally.
   */
  fun setLens(lens: CameraLens)

  /**
   * Release camera resources
   */
  fun release()

  // Extension Management

  /**
   * Register a camera control extension.
   *
   * @param extension The extension to register
   * @throws IllegalArgumentException if an extension with the same ID is already registered
   */
  fun registerExtension(extension: CameraControlExtension)

  /**
   * Unregister a camera control extension.
   *
   * @param id The ID of the extension to unregister
   */
  fun unregisterExtension(id: String)

  /**
   * Get a registered extension by ID.
   *
   * @param T The type of extension
   * @param id The ID of the extension
   * @return The extension, or null if not found
   */
  fun <T : CameraControlExtension> getExtension(id: String): T?

  // Preview Surface Provider

  /**
   * Set the preview surface provider for custom preview implementations.
   *
   * @param provider The provider, or null to clear
   */
  fun setPreviewSurfaceProvider(provider: PreviewSurfaceProvider?)
}

/**
 * Result of an image capture operation
 */
sealed class ImageCaptureResult {
  /**
   * Image capture succeeded
   */
  data class Success(
    val byteArray: ByteArray,
    val width: Int,
    val height: Int,
    val rotation: Int = 0,
    val filePath: String? = null,
  ) : ImageCaptureResult() {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || this::class != other::class) return false
      other as Success
      return byteArray.contentEquals(other.byteArray) &&
        width == other.width &&
        height == other.height &&
        rotation == other.rotation &&
        filePath == other.filePath
    }

    override fun hashCode(): Int {
      var result = byteArray.contentHashCode()
      result = 31 * result + width
      result = 31 * result + height
      result = 31 * result + rotation
      result = 31 * result + (filePath?.hashCode() ?: 0)
      return result
    }
  }

  /**
   * Image capture failed
   */
  data class Error(val exception: CameraException) : ImageCaptureResult()
}

/**
 * Handle for controlling an active video recording
 */
interface VideoRecording {
  /**
   * Whether the recording is currently active
   */
  val isRecording: Boolean

  /**
   * Stop the recording
   * @return The result of the recording operation
   */
  @NativeCoroutines
  suspend fun stop(): VideoRecordingResult

  /**
   * Pause the recording (if supported)
   */
  fun pause()

  /**
   * Resume a paused recording
   */
  fun resume()
}

/**
 * Result of a video recording operation
 */
sealed class VideoRecordingResult {
  /**
   * Recording succeeded
   */
  data class Success(
    val uri: String,
    val durationMs: Long,
  ) : VideoRecordingResult()

  /**
   * Recording failed
   */
  data class Error(val exception: CameraException) : VideoRecordingResult()
}
