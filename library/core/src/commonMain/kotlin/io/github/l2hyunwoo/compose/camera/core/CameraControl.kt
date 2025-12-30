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

/**
 * Interface defining camera control actions.
 */
interface CameraControl {
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
   * Set exposure compensation value.
   * @param exposureValue Exposure Value to set (will be clamped to supported range)
   */
  fun setExposureCompensation(exposureValue: Float)

  /**
   * Set the flash mode
   */
  fun setFlashMode(mode: FlashMode)

  /**
   * Enable or disable torch.
   */
  fun enableTorch(enabled: Boolean)
}
