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

import io.github.l2hyunwoo.compose.camera.core.CameraControl
import io.github.l2hyunwoo.compose.camera.core.FlashMode
import io.github.l2hyunwoo.compose.camera.core.FocusPoint

/**
 * Fake implementation of [CameraControl] for testing.
 * Records all operations for verification.
 */
class FakeCameraControl(
  val minZoomRatio: Float = 1.0f,
  maxZoomRatio: Float = 10.0f,
  val exposureRange: Pair<Float, Float> = -2.0f to 2.0f,
) : CameraControl {

  // Ensure maxZoomRatio is at least minZoomRatio
  val maxZoomRatio: Float = maxOf(maxZoomRatio, minZoomRatio)

  // Recorded state - initialize currentZoomRatio to minZoomRatio for consistency
  var currentZoomRatio: Float = minZoomRatio
    private set
  var currentLinearZoom: Float = 0.0f
    private set
  var currentFocusPoint: FocusPoint? = null
    private set
  var currentExposureCompensation: Float = 0.0f
    private set
  var currentFlashMode: FlashMode = FlashMode.OFF
    private set
  var isTorchEnabled: Boolean = false
    private set

  // Call tracking
  private val _setZoomCalls = mutableListOf<Float>()
  val setZoomCalls: List<Float> get() = _setZoomCalls

  private val _setLinearZoomCalls = mutableListOf<Float>()
  val setLinearZoomCalls: List<Float> get() = _setLinearZoomCalls

  private val _focusCalls = mutableListOf<FocusPoint>()
  val focusCalls: List<FocusPoint> get() = _focusCalls

  override fun setZoom(ratio: Float) {
    _setZoomCalls.add(ratio)
    currentZoomRatio = ratio.coerceIn(minZoomRatio, maxZoomRatio)
    // Update linear zoom based on ratio
    currentLinearZoom = if (maxZoomRatio > minZoomRatio) {
      ((currentZoomRatio - minZoomRatio) / (maxZoomRatio - minZoomRatio)).coerceIn(0f, 1f)
    } else {
      0f
    }
  }

  override fun setLinearZoom(linearZoom: Float) {
    _setLinearZoomCalls.add(linearZoom)
    currentLinearZoom = linearZoom.coerceIn(0f, 1f)
    // Update zoom ratio based on linear zoom
    currentZoomRatio = minZoomRatio + (maxZoomRatio - minZoomRatio) * currentLinearZoom
  }

  override fun focus(point: FocusPoint) {
    _focusCalls.add(point)
    currentFocusPoint = point
  }

  override fun setExposureCompensation(exposureValue: Float) {
    currentExposureCompensation = exposureValue.coerceIn(exposureRange.first, exposureRange.second)
  }

  override fun setFlashMode(mode: FlashMode) {
    currentFlashMode = mode
  }

  override fun enableTorch(enabled: Boolean) {
    isTorchEnabled = enabled
  }

  /**
   * Reset all recorded state for testing.
   */
  fun reset() {
    currentZoomRatio = minZoomRatio
    currentLinearZoom = 0.0f
    currentFocusPoint = null
    currentExposureCompensation = 0.0f
    currentFlashMode = FlashMode.OFF
    isTorchEnabled = false
    _setZoomCalls.clear()
    _setLinearZoomCalls.clear()
    _focusCalls.clear()
  }
}
