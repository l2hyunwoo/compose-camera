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
 * Capture mode for prioritizing quality or speed.
 */
enum class CaptureMode {
  /**
   * Optimize for image quality.
   * This may increase the time it takes to capture an image.
   *
   * - Android: Maps to [ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY]
   * - iOS: Maps to [AVCapturePhotoQualityPrioritizationQuality]
   */
  QUALITY,

  /**
   * Optimize for capture speed.
   * This may reduce image quality slightly.
   *
   * - Android: Maps to [ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY]
   * - iOS: Maps to [AVCapturePhotoQualityPrioritizationSpeed]
   */
  SPEED,

  /**
   * Balance between quality and speed.
   * This is the default mode.
   *
   * - Android: Maps to [ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY]
   * - iOS: Maps to [AVCapturePhotoQualityPrioritizationBalanced]
   */
  BALANCED,
}
