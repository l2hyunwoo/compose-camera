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
package io.github.l2hyunwoo.compose.camera.plugin

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.github.l2hyunwoo.compose.camera.CameraController
import kotlinx.coroutines.flow.Flow

/**
 * Base interface for camera plugins.
 * Plugins can extend camera functionality like QR scanning, OCR, face detection, etc.
 */
interface CameraPlugin {
  /**
   * Unique identifier for this plugin
   */
  val id: String

  /**
   * Called when the plugin is attached to a camera controller
   */
  fun onAttach(controller: CameraController)

  /**
   * Called when the plugin is detached from the camera controller
   */
  fun onDetach()
}

/**
 * Plugin that can analyze camera frames.
 * Implement this interface to process frames and produce results.
 *
 * @param T The type of result produced by the analyzer
 */
interface FrameAnalyzerPlugin<T> : CameraPlugin {
  /**
   * Analyze a camera frame and emit results.
   *
   * @param frame The camera frame to analyze
   * @return A Flow emitting analysis results
   */
  @NativeCoroutines
  fun analyze(frame: CameraFrame): Flow<T>

  /**
   * Start the analyzer
   */
  fun start()

  /**
   * Stop the analyzer
   */
  fun stop()

  /**
   * Whether the analyzer is currently running
   */
  val isRunning: Boolean
}

/**
 * Represents a camera frame for analysis.
 */
data class CameraFrame(
  /**
   * Raw frame data as ByteArray
   */
  val data: ByteArray,

  /**
   * Frame width in pixels
   */
  val width: Int,

  /**
   * Frame height in pixels
   */
  val height: Int,

  /**
   * Pixel format of the frame (e.g., "YUV_420_888", "RGBA_8888")
   */
  val format: String,

  /**
   * Rotation of the frame in degrees (0, 90, 180, 270)
   */
  val rotation: Int,

  /**
   * Timestamp of the frame in nanoseconds
   */
  val timestamp: Long,

  /**
   * Platform-specific image object (ImageProxy on Android, CMSampleBuffer on iOS)
   */
  val nativeImage: Any? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as CameraFrame
    return data.contentEquals(other.data) &&
      width == other.width &&
      height == other.height &&
      format == other.format &&
      rotation == other.rotation &&
      timestamp == other.timestamp
  }

  override fun hashCode(): Int {
    var result = data.contentHashCode()
    result = 31 * result + width
    result = 31 * result + height
    result = 31 * result + format.hashCode()
    result = 31 * result + rotation
    result = 31 * result + timestamp.hashCode()
    return result
  }
}

/**
 * Plugin that can be notified of image capture events.
 */
interface ImageCapturePlugin : CameraPlugin {
  /**
   * Called when an image is captured
   *
   * @param imageData The captured image data
   * @param width Image width
   * @param height Image height
   */
  fun onImageCaptured(imageData: ByteArray, width: Int, height: Int)
}
