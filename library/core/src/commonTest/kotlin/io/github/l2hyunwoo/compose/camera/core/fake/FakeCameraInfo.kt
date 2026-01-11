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

import io.github.l2hyunwoo.compose.camera.core.CameraInfo
import io.github.l2hyunwoo.compose.camera.core.CameraLens
import io.github.l2hyunwoo.compose.camera.core.CameraState
import io.github.l2hyunwoo.compose.camera.core.ExposureState
import io.github.l2hyunwoo.compose.camera.core.FlashMode
import io.github.l2hyunwoo.compose.camera.core.Resolution
import io.github.l2hyunwoo.compose.camera.core.ZoomState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake implementation of [CameraInfo] for testing.
 * All state properties are mutable for test setup.
 */
class FakeCameraInfo(
  initialState: CameraState = CameraState.Initializing,
  initialZoomState: ZoomState = ZoomState(),
  initialExposureState: ExposureState = ExposureState(),
  override val hasFlashUnit: Boolean = true,
  override val hasTorch: Boolean = true,
  override val supportedPhotoResolutions: List<Resolution> = listOf(
    Resolution(4032, 3024),
    Resolution(1920, 1080),
  ),
  override val supportedVideoResolutions: List<Resolution> = listOf(
    Resolution(1920, 1080),
    Resolution(1280, 720),
  ),
) : CameraInfo {

  private val _cameraState = MutableStateFlow(initialState)
  override val cameraState: StateFlow<CameraState> = _cameraState

  private val _zoomState = MutableStateFlow(initialZoomState)
  override val zoomState: StateFlow<ZoomState> = _zoomState

  private val _exposureState = MutableStateFlow(initialExposureState)
  override val exposureState: StateFlow<ExposureState> = _exposureState

  /**
   * Set the camera state for testing.
   */
  fun setCameraState(state: CameraState) {
    _cameraState.value = state
  }

  /**
   * Set the zoom state for testing.
   */
  fun setZoomState(state: ZoomState) {
    _zoomState.value = state
  }

  /**
   * Set the exposure state for testing.
   */
  fun setExposureState(state: ExposureState) {
    _exposureState.value = state
  }

  /**
   * Transition to Ready state with default values.
   */
  fun transitionToReady(
    lens: CameraLens = CameraLens.BACK,
    flashMode: FlashMode = FlashMode.OFF,
  ) {
    _cameraState.value = CameraState.Ready(
      currentLens = lens,
      flashMode = flashMode,
    )
  }
}
