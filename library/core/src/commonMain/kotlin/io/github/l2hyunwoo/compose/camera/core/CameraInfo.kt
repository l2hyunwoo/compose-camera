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

import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining read-only camera state.
 */
interface CameraInfo {
  /**
   * Current camera state as a StateFlow.
   */
  @NativeCoroutinesState
  val cameraState: StateFlow<CameraState>

  /**
   * Current zoom state.
   */
  @NativeCoroutinesState
  val zoomState: StateFlow<ZoomState>

  /**
   * Current exposure state.
   */
  @NativeCoroutinesState
  val exposureState: StateFlow<ExposureState>

  /**
   * Current flash mode.
   */
  val flashMode: FlashMode
    get() {
      val state = cameraState.value
      return if (state is CameraState.Ready) state.flashMode else FlashMode.OFF
    }

  /**
   * List of supported photo resolutions for current camera.
   */
  val supportedPhotoResolutions: List<Resolution>

  /**
   * List of supported video resolutions for current camera.
   */
  val supportedVideoResolutions: List<Resolution>
}

/**
 * Data class holding zoom information.
 */
data class ZoomState(
  val zoomRatio: Float = 1.0f,
  val minZoomRatio: Float = 1.0f,
  val maxZoomRatio: Float = 1.0f,
) {
  /**
   * Linear zoom value (0.0 to 1.0), derived from zoomRatio.
   * 0.0 = minimum zoom, 1.0 = maximum zoom
   */
  val linearZoom: Float
    get() = if (maxZoomRatio > minZoomRatio) {
      ((zoomRatio - minZoomRatio) / (maxZoomRatio - minZoomRatio)).coerceIn(0f, 1f)
    } else {
      0f
    }
}

/**
 * Data class holding exposure information.
 */
data class ExposureState(
  val exposureCompensation: Float = 0.0f,
  val exposureCompensationRange: Pair<Float, Float> = Pair(0.0f, 0.0f),
  val exposureStep: Float = 0.0f,
)
