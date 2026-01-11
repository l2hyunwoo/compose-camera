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

/**
 * Interface for customizing image capture behavior.
 * Implement this to create custom capture logic (RAW capture, burst mode, HDR stacking, etc.)
 *
 * Example:
 * ```kotlin
 * class RawCaptureUseCase : ImageCaptureUseCase {
 *     override suspend fun capture(
 *         controller: CameraController,
 *         config: CaptureConfig
 *     ): ImageCaptureResult {
 *         val androidController = controller as AndroidCameraController
 *         // Custom RAW capture logic using CameraX ImageCapture
 *     }
 * }
 * ```
 */
interface ImageCaptureUseCase {
  /**
   * Capture an image with the given configuration.
   *
   * @param controller The camera controller to capture from
   * @param config Configuration for the capture operation
   * @return Result of the capture operation
   */
  suspend fun capture(controller: CameraController, config: CaptureConfig): ImageCaptureResult
}

/**
 * Interface for customizing video recording behavior.
 * Implement this to create custom recording logic (time-lapse, slow-motion, custom encoding, etc.)
 */
interface VideoCaptureUseCase {
  /**
   * Start a video recording with the given configuration.
   *
   * @param controller The camera controller to record from
   * @param config Configuration for the recording operation
   * @return A handle to control the active recording
   */
  suspend fun startRecording(controller: CameraController, config: RecordingConfig): VideoRecording
}

/**
 * Configuration for image capture operations.
 */
data class CaptureConfig(
  val flashMode: FlashMode = FlashMode.OFF,
  val resolution: Resolution? = null,
  val outputFormat: ImageFormat = ImageFormat.JPEG,
  /**
   * Platform-specific options.
   * Android: JPEG quality, rotation, etc.
   * iOS: AVCapturePhotoSettings options
   */
  val extras: Map<String, Any> = emptyMap(),
)

/**
 * Configuration for video recording operations.
 */
data class RecordingConfig(
  val quality: VideoQuality = VideoQuality.FHD,
  val enableAudio: Boolean = true,
  val maxDurationMs: Long? = null,
  val maxFileSizeBytes: Long? = null,
  /**
   * Platform-specific options.
   */
  val extras: Map<String, Any> = emptyMap(),
)
