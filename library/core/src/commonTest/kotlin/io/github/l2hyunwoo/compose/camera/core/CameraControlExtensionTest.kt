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

import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraControlExtension.LifecycleEvent
import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraController
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class CameraControlExtensionTest :
  FunSpec({

    lateinit var controller: FakeCameraController
    lateinit var extension: FakeCameraControlExtension

    beforeTest {
      controller = FakeCameraController()
      extension = FakeCameraControlExtension(id = "test-extension")
    }

    context("Extension Lifecycle Tests") {
      test("onAttach called when extension registered") {
        controller.registerExtension(extension)

        extension.onAttachCallCount shouldBe 1
        extension.attachedController shouldBe controller
        extension.isAttached shouldBe true
      }

      test("onDetach called when extension unregistered") {
        controller.registerExtension(extension)
        controller.unregisterExtension(extension.id)

        extension.onDetachCallCount shouldBe 1
        extension.attachedController shouldBe null
        extension.isAttached shouldBe false
      }

      test("onCameraReady called after controller initialized") {
        controller.registerExtension(extension)
        extension.onCameraReadyCallCount shouldBe 0

        controller.initialize()

        extension.onCameraReadyCallCount shouldBe 1
      }

      test("onCameraReady called immediately if registered after initialization") {
        controller.initialize()
        controller.registerExtension(extension)

        extension.onCameraReadyCallCount shouldBe 1
      }

      test("onCameraReleased called before controller released") {
        controller.registerExtension(extension)
        controller.initialize()
        controller.release()

        extension.onCameraReleasedCallCount shouldBe 1
      }

      test("extension lifecycle order is correct") {
        controller.registerExtension(extension)
        controller.initialize()
        controller.release()

        extension.lifecycleCalls shouldBe listOf(
          LifecycleEvent.ATTACH,
          LifecycleEvent.CAMERA_READY,
          LifecycleEvent.CAMERA_RELEASED,
          LifecycleEvent.DETACH,
        )
      }
    }

    context("Extension Registry Tests") {
      test("registerExtension adds to registry") {
        controller.registerExtension(extension)

        val retrieved = controller.getExtension<FakeCameraControlExtension>(extension.id)
        retrieved.shouldNotBeNull()
        retrieved shouldBe extension
      }

      test("unregisterExtension removes from registry") {
        controller.registerExtension(extension)
        controller.unregisterExtension(extension.id)

        val retrieved = controller.getExtension<FakeCameraControlExtension>(extension.id)
        retrieved.shouldBeNull()
      }

      test("duplicate extension registration throws exception") {
        controller.registerExtension(extension)

        shouldThrow<IllegalArgumentException> {
          controller.registerExtension(extension)
        }
      }

      test("getExtension returns null for unregistered type") {
        val retrieved = controller.getExtension<FakeCameraControlExtension>("non-existent")
        retrieved.shouldBeNull()
      }

      test("multiple extensions can be registered") {
        val extension1 = FakeCameraControlExtension(id = "ext-1")
        val extension2 = FakeCameraControlExtension(id = "ext-2")

        controller.registerExtension(extension1)
        controller.registerExtension(extension2)

        controller.getExtension<FakeCameraControlExtension>("ext-1") shouldBe extension1
        controller.getExtension<FakeCameraControlExtension>("ext-2") shouldBe extension2
      }

      test("unregister nonexistent extension is safe") {
        // Should not throw
        controller.unregisterExtension("non-existent")
      }

      test("all extensions are detached on release") {
        val extension1 = FakeCameraControlExtension(id = "ext-1")
        val extension2 = FakeCameraControlExtension(id = "ext-2")

        controller.registerExtension(extension1)
        controller.registerExtension(extension2)
        controller.initialize()
        controller.release()

        extension1.onDetachCallCount shouldBe 1
        extension2.onDetachCallCount shouldBe 1
      }
    }

    context("Extension registration after release") {
      test("cannot register extension after release") {
        controller.release()

        shouldThrow<IllegalStateException> {
          controller.registerExtension(extension)
        }
      }

      test("cannot unregister extension after release") {
        controller.registerExtension(extension)
        controller.release()

        shouldThrow<IllegalStateException> {
          controller.unregisterExtension(extension.id)
        }
      }
    }
  })
