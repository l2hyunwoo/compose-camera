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
package io.github.l2hyunwoo.camera.core

/**
 * Represents the current state of the camera
 */
sealed class CameraState {
  /**
   * Camera is initializing
   */
  data object Initializing : CameraState()

  /**
   * Camera is ready and preview is active
   */
  data class Ready(
    val currentLens: CameraLens,
    val flashMode: FlashMode,
    val isRecording: Boolean = false,
    val zoomRatio: Float = 1.0f,
  ) : CameraState()

  /**
   * Camera encountered an error
   */
  data class Error(val exception: CameraException) : CameraState()
}

/**
 * Camera-related exceptions
 */
sealed class CameraException(
  message: String,
  cause: Throwable? = null,
) : Exception(message, cause) {
  /**
   * Camera permission was not granted
   */
  class PermissionDenied : CameraException("Camera permission denied")

  /**
   * No camera device available
   */
  class NoCameraAvailable : CameraException("No camera device available")

  /**
   * Camera initialization failed
   */
  class InitializationFailed(cause: Throwable? = null) :
    CameraException("Camera initialization failed", cause)

  /**
   * Image capture failed
   */
  class CaptureFailed(cause: Throwable? = null) :
    CameraException("Image capture failed", cause)

  /**
   * Video recording failed
   */
  class RecordingFailed(cause: Throwable? = null) :
    CameraException("Video recording failed", cause)

  /**
   * Unknown camera error
   */
  class Unknown(message: String, cause: Throwable? = null) :
    CameraException(message, cause)
}
