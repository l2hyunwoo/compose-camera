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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraStateTest {
  @Test
  fun testInitializingState() {
    val state = CameraState.Initializing
    assertTrue(state is CameraState.Initializing)
  }

  @Test
  fun testReadyState() {
    val state = CameraState.Ready(
      currentLens = CameraLens.BACK,
      flashMode = FlashMode.OFF,
      isRecording = false,
      zoomRatio = 1.0f,
    )

    assertEquals(CameraLens.BACK, state.currentLens)
    assertEquals(FlashMode.OFF, state.flashMode)
    assertEquals(false, state.isRecording)
    assertEquals(1.0f, state.zoomRatio)
  }

  @Test
  fun testErrorState() {
    val exception = CameraException.PermissionDenied()
    val state = CameraState.Error(exception)

    assertTrue(state.exception is CameraException.PermissionDenied)
  }

  @Test
  fun testReadyStateWithCustomZoom() {
    val state = CameraState.Ready(
      currentLens = CameraLens.BACK,
      flashMode = FlashMode.OFF,
      zoomRatio = 2.5f,
    )

    assertEquals(2.5f, state.zoomRatio)
  }

  @Test
  fun testCopyStateWithNewZoom() {
    val original = CameraState.Ready(
      currentLens = CameraLens.FRONT,
      flashMode = FlashMode.ON,
      zoomRatio = 1.0f,
    )

    val zoomed = original.copy(zoomRatio = 3.0f)

    assertEquals(3.0f, zoomed.zoomRatio)
    assertEquals(original.currentLens, zoomed.currentLens)
    assertEquals(original.flashMode, zoomed.flashMode)
    assertEquals(original.isRecording, zoomed.isRecording)
  }

  @Test
  fun testReadyStateWithExposureCompensation() {
    val state = CameraState.Ready(
      currentLens = CameraLens.BACK,
      flashMode = FlashMode.OFF,
      exposureCompensation = 1.5f,
    )

    assertEquals(1.5f, state.exposureCompensation)
  }

  @Test
  fun testDefaultExposureCompensation() {
    val state = CameraState.Ready(
      currentLens = CameraLens.BACK,
      flashMode = FlashMode.OFF,
    )

    assertEquals(0.0f, state.exposureCompensation)
  }

  @Test
  fun testCopyStateWithNewExposureCompensation() {
    val original = CameraState.Ready(
      currentLens = CameraLens.FRONT,
      flashMode = FlashMode.ON,
      exposureCompensation = 0.0f,
    )

    val adjusted = original.copy(exposureCompensation = -1.5f)

    assertEquals(-1.5f, adjusted.exposureCompensation)
    assertEquals(original.currentLens, adjusted.currentLens)
    assertEquals(original.flashMode, adjusted.flashMode)
    assertEquals(original.zoomRatio, adjusted.zoomRatio)
  }
}
