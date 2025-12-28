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
package io.github.l2hyunwoo.camera.compose

/**
 * Compose Camera - A camera library for Compose Multiplatform
 *
 * This library provides a unified camera API for Android and iOS platforms,
 * with support for:
 * - Camera preview
 * - Image capture
 * - Video recording
 * - Frame processing (for ML Kit, custom filters, etc.)
 * - Custom plugins
 *
 * ## Usage
 *
 * ```kotlin
 * // Basic preview
 * CameraPreview(
 *     modifier = Modifier.fillMaxSize(),
 *     configuration = CameraConfiguration(lens = CameraLens.BACK),
 *     onCameraControllerReady = { controller ->
 *         // Use controller
 *     }
 * )
 *
 * // Take a picture
 * val result = controller.takePicture()
 * when (result) {
 *     is ImageCaptureResult.Success -> { /* handle success */ }
 *     is ImageCaptureResult.Error -> { /* handle error */ }
 * }
 * ```
 *
 * ## Plugins
 *
 * Add plugins for extended functionality:
 * - `compose-camera-mlkit-barcode` - QR/Barcode scanning
 * - `compose-camera-mlkit-ocr` - Text recognition
 * - `compose-camera-mlkit-face` - Face detection
 */
object ComposeCamera {
  const val VERSION = "1.0.0-alpha01"
}
