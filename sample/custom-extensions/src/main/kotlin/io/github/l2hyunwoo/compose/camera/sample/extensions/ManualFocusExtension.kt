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
package io.github.l2hyunwoo.compose.camera.sample.extensions

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.CameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.CameraController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extension that enables manual focus control.
 *
 * This demonstrates how to create an extension that:
 * - Uses Camera2 Interop to access low-level camera controls
 * - Disables auto-focus and enables manual focus distance control
 * - Exposes focus state via StateFlow
 *
 * Note: Manual focus is only supported on devices that report
 * LENS_INFO_MINIMUM_FOCUS_DISTANCE > 0.
 */
@OptIn(ExperimentalCamera2Interop::class)
class ManualFocusExtension : CameraControlExtension {
  override val id: String = "manual-focus"

  private var controller: AndroidCameraController? = null
  private var camera2Control: Camera2CameraControl? = null
  private var camera2Info: Camera2CameraInfo? = null

  private val _isManualFocusEnabled = MutableStateFlow(false)
  val isManualFocusEnabled: StateFlow<Boolean> = _isManualFocusEnabled.asStateFlow()

  private val _focusDistance = MutableStateFlow(0f)
  val focusDistance: StateFlow<Float> = _focusDistance.asStateFlow()

  private val _minFocusDistance = MutableStateFlow(0f)
  val minFocusDistance: StateFlow<Float> = _minFocusDistance.asStateFlow()

  private val _isSupported = MutableStateFlow(false)
  val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

  override fun onAttach(controller: CameraController) {
    if (controller is AndroidCameraController) {
      this.controller = controller
    }
  }

  override fun onDetach() {
    disableManualFocus()
    controller = null
    camera2Control = null
    camera2Info = null
  }

  override fun onCameraReady() {
    val ctrl = controller ?: return

    // Access Camera2 interop APIs
    val cameraInfo = ctrl.cameraInfo
    // We need to get the internal camera object - accessing via reflection or exposed API
    // For now, we'll get it when the camera is ready via the controller's internal camera
    try {
      val cameraField = ctrl::class.java.getDeclaredField("camera")
      cameraField.isAccessible = true
      val camera = cameraField.get(ctrl) as? androidx.camera.core.Camera ?: return

      camera2Info = Camera2CameraInfo.from(camera.cameraInfo)
      camera2Control = Camera2CameraControl.from(camera.cameraControl)

      // Check if manual focus is supported
      val minFocusDist = camera2Info?.getCameraCharacteristic(
        CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE,
      ) ?: 0f

      _minFocusDistance.value = minFocusDist
      _isSupported.value = minFocusDist > 0f

      // Initialize focus distance to hyperfocal (infinity)
      _focusDistance.value = 0f
    } catch (e: Exception) {
      _isSupported.value = false
    }
  }

  override fun onCameraReleased() {
    _isManualFocusEnabled.value = false
    _focusDistance.value = 0f
    camera2Control = null
    camera2Info = null
  }

  /**
   * Enable manual focus mode.
   * This disables auto-focus and allows manual control of focus distance.
   */
  fun enableManualFocus() {
    if (!_isSupported.value) return

    val control = camera2Control ?: return

    val options = CaptureRequestOptions.Builder()
      .setCaptureRequestOption(
        CaptureRequest.CONTROL_AF_MODE,
        CaptureRequest.CONTROL_AF_MODE_OFF,
      )
      .setCaptureRequestOption(
        CaptureRequest.LENS_FOCUS_DISTANCE,
        _focusDistance.value,
      )
      .build()

    control.captureRequestOptions = options
    _isManualFocusEnabled.value = true
  }

  /**
   * Disable manual focus and return to auto-focus mode.
   */
  fun disableManualFocus() {
    val control = camera2Control ?: return

    val options = CaptureRequestOptions.Builder()
      .setCaptureRequestOption(
        CaptureRequest.CONTROL_AF_MODE,
        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO,
      )
      .clearCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE)
      .build()

    control.captureRequestOptions = options
    _isManualFocusEnabled.value = false
  }

  /**
   * Toggle between manual and auto focus modes.
   */
  fun toggle() {
    if (_isManualFocusEnabled.value) {
      disableManualFocus()
    } else {
      enableManualFocus()
    }
  }

  /**
   * Set the focus distance.
   *
   * @param distance Focus distance in diopters (1/meters).
   *                 0 = infinity focus
   *                 [minFocusDistance] = closest focus (macro)
   *
   * Only works when manual focus is enabled.
   */
  fun setFocusDistance(distance: Float) {
    if (!_isManualFocusEnabled.value) return

    val control = camera2Control ?: return
    val clampedDistance = distance.coerceIn(0f, _minFocusDistance.value)

    val options = CaptureRequestOptions.Builder()
      .setCaptureRequestOption(
        CaptureRequest.CONTROL_AF_MODE,
        CaptureRequest.CONTROL_AF_MODE_OFF,
      )
      .setCaptureRequestOption(
        CaptureRequest.LENS_FOCUS_DISTANCE,
        clampedDistance,
      )
      .build()

    control.captureRequestOptions = options
    _focusDistance.value = clampedDistance
  }

  /**
   * Focus distance as a normalized value (0-1).
   * 0 = infinity, 1 = macro (closest)
   */
  fun setNormalizedFocusDistance(normalized: Float) {
    val distance = normalized.coerceIn(0f, 1f) * _minFocusDistance.value
    setFocusDistance(distance)
  }

  /**
   * Get current focus distance as normalized value (0-1).
   */
  fun getNormalizedFocusDistance(): Float {
    val minDist = _minFocusDistance.value
    return if (minDist > 0f) {
      _focusDistance.value / minDist
    } else {
      0f
    }
  }
}
