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

import androidx.compose.ui.geometry.Offset
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.StateFlow

/**
 * Controller for camera operations.
 * Provides methods for capturing photos, recording videos, and controlling camera settings.
 */
interface CameraController {
  /**
   * Current camera state as a StateFlow.
   * Annotated with [NativeCoroutinesState] for Swift compatibility.
   */
  @NativeCoroutinesState
  val cameraState: StateFlow<CameraState>

  /**
   * Current zoom ratio as a StateFlow.
   * This provides real-time updates of the zoom level (1.0 = no zoom).
   * Annotated with [NativeCoroutinesState] for Swift compatibility.
   */
  @NativeCoroutinesState
  val zoomRatioFlow: StateFlow<Float>

  /**
   * Minimum zoom ratio supported by the camera.
   * This is hardware-dependent and typically 1.0f for most devices.
   * Some devices with wide-angle lenses may support values less than 1.0f.
   */
  val minZoomRatio: Float

  /**
   * Maximum zoom ratio supported by the camera.
   * This is hardware-dependent and varies between devices.
   */
  val maxZoomRatio: Float

  /**
   * Current camera configuration
   */
  val configuration: CameraConfiguration

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
   */
  fun setLens(lens: CameraLens)

  /**
   * Set the flash mode
   */
  fun setFlashMode(mode: FlashMode)

  /**
   * Set the zoom ratio (1.0 = no zoom)
   */
  fun setZoom(ratio: Float)

  /**
   * Focus on a specific point in the preview
   * @param point The normalized point (0-1 range) to focus on
   */
  fun focus(point: Offset)

  /**
   * Release camera resources
   */
  fun release()
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
