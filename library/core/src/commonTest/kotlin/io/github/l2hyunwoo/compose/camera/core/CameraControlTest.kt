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

import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraControl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.shouldBe

class CameraControlTest :
  FunSpec({

    lateinit var control: FakeCameraControl

    beforeTest {
      control = FakeCameraControl(
        minZoomRatio = 1.0f,
        maxZoomRatio = 10.0f,
        exposureRange = -2.0f to 2.0f,
      )
    }

    context("Zoom Tests") {
      test("setZoom clamps to min ratio") {
        control.setZoom(0.5f)
        control.currentZoomRatio shouldBeExactly 1.0f
      }

      test("setZoom clamps to max ratio") {
        control.setZoom(15.0f)
        control.currentZoomRatio shouldBeExactly 10.0f
      }

      test("setZoom accepts valid ratio") {
        control.setZoom(5.0f)
        control.currentZoomRatio shouldBeExactly 5.0f
      }

      test("setLinearZoom 0 sets minimum zoom") {
        control.setLinearZoom(0f)
        control.currentLinearZoom shouldBeExactly 0f
        control.currentZoomRatio shouldBeExactly 1.0f
      }

      test("setLinearZoom 1 sets maximum zoom") {
        control.setLinearZoom(1f)
        control.currentLinearZoom shouldBeExactly 1f
        control.currentZoomRatio shouldBeExactly 10.0f
      }

      test("setLinearZoom clamps values below 0") {
        control.setLinearZoom(-0.5f)
        control.currentLinearZoom shouldBeExactly 0f
      }

      test("setLinearZoom clamps values above 1") {
        control.setLinearZoom(1.5f)
        control.currentLinearZoom shouldBeExactly 1f
      }

      test("setLinearZoom accepts valid mid value") {
        control.setLinearZoom(0.5f)
        control.currentLinearZoom shouldBeExactly 0.5f
        // At linear 0.5, zoom ratio should be (1 + 10) / 2 = 5.5
        control.currentZoomRatio shouldBeExactly 5.5f
      }
    }

    context("Exposure Tests") {
      test("setExposureCompensation accepts valid value") {
        control.setExposureCompensation(1.0f)
        control.currentExposureCompensation shouldBeExactly 1.0f
      }

      test("setExposureCompensation clamps below min") {
        control.setExposureCompensation(-5.0f)
        control.currentExposureCompensation shouldBeExactly -2.0f
      }

      test("setExposureCompensation clamps above max") {
        control.setExposureCompensation(5.0f)
        control.currentExposureCompensation shouldBeExactly 2.0f
      }
    }

    context("Flash and Torch Tests") {
      test("setFlashMode updates flash mode") {
        control.setFlashMode(FlashMode.ON)
        control.currentFlashMode shouldBe FlashMode.ON

        control.setFlashMode(FlashMode.AUTO)
        control.currentFlashMode shouldBe FlashMode.AUTO
      }

      test("enableTorch updates torch state") {
        control.enableTorch(true)
        control.isTorchEnabled shouldBe true

        control.enableTorch(false)
        control.isTorchEnabled shouldBe false
      }
    }

    context("Focus Tests") {
      test("focus with FocusPoint records call") {
        val point = FocusPoint(0.3f, 0.7f)
        control.focus(point)

        control.currentFocusPoint shouldBe point
        control.focusCalls.size shouldBe 1
        control.focusCalls[0] shouldBe point
      }

      test("focus records CENTER point") {
        control.focus(FocusPoint.CENTER)

        control.currentFocusPoint shouldBe FocusPoint.CENTER
      }
    }

    context("Call tracking") {
      test("zoom calls are recorded") {
        control.setZoom(2.0f)
        control.setZoom(3.0f)
        control.setZoom(4.0f)

        control.setZoomCalls shouldBe listOf(2.0f, 3.0f, 4.0f)
      }

      test("linearZoom calls are recorded") {
        control.setLinearZoom(0.2f)
        control.setLinearZoom(0.5f)

        control.setLinearZoomCalls shouldBe listOf(0.2f, 0.5f)
      }

      test("reset clears all state") {
        control.setZoom(5.0f)
        control.setLinearZoom(0.5f)
        control.setFlashMode(FlashMode.ON)
        control.enableTorch(true)
        control.focus(FocusPoint.CENTER)

        control.reset()

        control.currentZoomRatio shouldBeExactly 1.0f
        control.currentLinearZoom shouldBeExactly 0.0f
        control.currentFlashMode shouldBe FlashMode.OFF
        control.isTorchEnabled shouldBe false
        control.currentFocusPoint shouldBe null
        control.setZoomCalls shouldBe emptyList()
        control.focusCalls shouldBe emptyList()
      }
    }
  })
