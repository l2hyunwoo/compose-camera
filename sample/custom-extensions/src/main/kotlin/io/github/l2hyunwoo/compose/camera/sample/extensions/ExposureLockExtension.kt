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

import io.github.l2hyunwoo.compose.camera.core.CameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.CameraController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extension that allows locking the current exposure value.
 *
 * This demonstrates how to create a custom extension that:
 * - Maintains its own state (locked/unlocked)
 * - Interacts with the camera controller
 * - Exposes observable state via StateFlow
 *
 * Note: This is a demonstration extension that tracks lock state.
 * It stores the current exposure value but does not enforce it on the camera.
 * To actually prevent auto-exposure changes, you would need to use
 * platform-specific APIs (e.g., AVCaptureDevice.exposureMode on iOS,
 * or Camera2 CONTROL_AE_LOCK on Android).
 */
class ExposureLockExtension : CameraControlExtension {
  override val id: String = "exposure-lock"

  private var controller: CameraController? = null
  private var lockedExposureValue: Float? = null

  private val _isLocked = MutableStateFlow(false)
  val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

  override fun onAttach(controller: CameraController) {
    this.controller = controller
  }

  override fun onDetach() {
    unlock()
    controller = null
  }

  override fun onCameraReady() {
    // Camera is ready, we can now interact with it
  }

  override fun onCameraReleased() {
    unlock()
  }

  /**
   * Lock the current exposure value.
   *
   * Note: This stores the current exposure compensation value for tracking purposes.
   * It does NOT actually lock the camera's auto-exposure. To implement true AE lock,
   * you would need platform-specific code (Camera2 CONTROL_AE_LOCK or AVCaptureDevice.exposureMode).
   */
  fun lock() {
    controller?.let { ctrl ->
      // Store current exposure value for tracking (demonstration only)
      lockedExposureValue = ctrl.cameraInfo.exposureState.value.exposureCompensation
      _isLocked.value = true
    }
  }

  /**
   * Unlock the exposure, allowing it to adjust automatically again.
   */
  fun unlock() {
    lockedExposureValue = null
    _isLocked.value = false
  }

  /**
   * Toggle the lock state.
   */
  fun toggle() {
    if (_isLocked.value) {
      unlock()
    } else {
      lock()
    }
  }

  /**
   * Get the locked exposure value, if any.
   */
  fun getLockedValue(): Float? = lockedExposureValue
}
