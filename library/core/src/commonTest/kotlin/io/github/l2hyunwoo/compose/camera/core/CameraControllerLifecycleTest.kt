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

import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraController
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CameraControllerLifecycleTest :
  FunSpec({

    lateinit var controller: FakeCameraController

    beforeTest {
      controller = FakeCameraController()
    }

    context("CameraController Lifecycle Tests") {

      test("initialize transitions state from NotInitialized to Ready") {
        // Given
        controller.controllerState shouldBe FakeCameraController.ControllerState.NOT_INITIALIZED
        controller.cameraInfo.cameraState.value.shouldBeInstanceOf<CameraState.Initializing>()

        // When
        controller.initialize()

        // Then
        controller.controllerState shouldBe FakeCameraController.ControllerState.READY
        controller.cameraInfo.cameraState.value.shouldBeInstanceOf<CameraState.Ready>()
      }

      test("release transitions state to Released") {
        // Given
        controller.initialize()
        controller.controllerState shouldBe FakeCameraController.ControllerState.READY

        // When
        controller.release()

        // Then
        controller.controllerState shouldBe FakeCameraController.ControllerState.RELEASED
      }

      test("release after Released is no-op") {
        // Given
        controller.initialize()
        controller.release()
        val releaseCountAfterFirst = controller.releaseCallCount

        // When
        controller.release()

        // Then - release was only called once (no-op on second call)
        controller.releaseCallCount shouldBe releaseCountAfterFirst
        controller.controllerState shouldBe FakeCameraController.ControllerState.RELEASED
      }

      test("operations after release throw IllegalStateException") {
        // Given
        controller.initialize()
        controller.release()

        // Then
        shouldThrow<IllegalStateException> {
          controller.initialize()
        }

        shouldThrow<IllegalStateException> {
          controller.takePicture()
        }

        shouldThrow<IllegalStateException> {
          controller.startRecording()
        }

        shouldThrow<IllegalStateException> {
          controller.setLens(CameraLens.FRONT)
        }

        shouldThrow<IllegalStateException> {
          controller.updateConfiguration(CameraConfiguration())
        }
      }
    }

    context("Start/Stop Control Tests") {

      test("start after initialize begins streaming") {
        // Given
        controller.initialize()

        // When
        controller.start()

        // Then
        controller.controllerState shouldBe FakeCameraController.ControllerState.READY
        controller.startCallCount shouldBe 1
      }

      test("stop pauses streaming but keeps resources") {
        // Given
        controller.initialize()
        controller.start()

        // When
        controller.stop()

        // Then
        controller.controllerState shouldBe FakeCameraController.ControllerState.STOPPED
        controller.stopCallCount shouldBe 1
        // Controller is not released
        controller.releaseCallCount shouldBe 0
      }

      test("start after stop resumes streaming") {
        // Given
        controller.initialize()
        controller.start()
        controller.stop()
        controller.controllerState shouldBe FakeCameraController.ControllerState.STOPPED

        // When
        controller.start()

        // Then
        controller.controllerState shouldBe FakeCameraController.ControllerState.READY
        controller.startCallCount shouldBe 2
      }

      test("stop is resumable but release is not") {
        // Test stop is resumable
        controller.initialize()
        controller.stop()

        // Can start again after stop
        controller.start()
        controller.controllerState shouldBe FakeCameraController.ControllerState.READY

        // Test release is not resumable
        controller.release()

        // Cannot start after release
        shouldThrow<IllegalStateException> {
          controller.start()
        }
      }
    }

    context("Error Handling") {

      test("initialize can throw exception") {
        // Given
        val expectedException = CameraException.InitializationFailed(RuntimeException("Test error"))
        controller.initializeException = expectedException

        // When/Then
        shouldThrow<CameraException.InitializationFailed> {
          controller.initialize()
        }
      }

      test("takePicture requires initialization") {
        // Given - controller not initialized

        // When/Then
        shouldThrow<IllegalStateException> {
          controller.takePicture()
        }
      }

      test("startRecording requires initialization") {
        // Given - controller not initialized

        // When/Then
        shouldThrow<IllegalStateException> {
          controller.startRecording()
        }
      }
    }
  })
