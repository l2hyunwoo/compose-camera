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
import io.github.l2hyunwoo.compose.camera.core.fake.FakePreviewSurfaceProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PreviewSurfaceProviderTest :
  FunSpec({

    lateinit var controller: FakeCameraController
    lateinit var provider: FakePreviewSurfaceProvider

    beforeTest {
      controller = FakeCameraController()
      provider = FakePreviewSurfaceProvider()
    }

    context("Common Interface Tests") {
      test("setPreviewSurfaceProvider accepts provider") {
        controller.setPreviewSurfaceProvider(provider)

        controller.previewSurfaceProvider shouldBe provider
      }

      test("null provider clears existing provider") {
        controller.setPreviewSurfaceProvider(provider)
        controller.previewSurfaceProvider.shouldNotBeNull()

        controller.setPreviewSurfaceProvider(null)
        controller.previewSurfaceProvider.shouldBeNull()
      }

      test("previous provider is destroyed when new provider is set") {
        val provider1 = FakePreviewSurfaceProvider()
        val provider2 = FakePreviewSurfaceProvider()

        controller.setPreviewSurfaceProvider(provider1)
        controller.setPreviewSurfaceProvider(provider2)

        provider1.isDestroyed shouldBe true
        provider1.onSurfaceDestroyedCallCount shouldBe 1
      }
    }

    context("Surface lifecycle") {
      test("onSurfaceAvailable called when camera ready and provider set") {
        controller.initialize()
        controller.setPreviewSurfaceProvider(provider)

        provider.onSurfaceAvailableCallCount shouldBe 1
        provider.lastSurface.shouldNotBeNull()
      }

      test("onSurfaceAvailable not called before camera initialized") {
        controller.setPreviewSurfaceProvider(provider)

        provider.onSurfaceAvailableCallCount shouldBe 0
      }

      test("onSurfaceDestroyed called when controller released") {
        controller.initialize()
        controller.setPreviewSurfaceProvider(provider)
        controller.release()

        provider.isDestroyed shouldBe true
        provider.onSurfaceDestroyedCallCount shouldBe 1
      }

      test("surface lifecycle tracks multiple surfaces") {
        val provider1 = FakePreviewSurfaceProvider()
        val provider2 = FakePreviewSurfaceProvider()

        controller.initialize()

        // Set first provider
        controller.setPreviewSurfaceProvider(provider1)
        provider1.onSurfaceAvailableCallCount shouldBe 1

        // Replace with second provider
        controller.setPreviewSurfaceProvider(provider2)
        provider1.onSurfaceDestroyedCallCount shouldBe 1
        provider2.onSurfaceAvailableCallCount shouldBe 1

        // Clear provider
        controller.setPreviewSurfaceProvider(null)
        provider2.onSurfaceDestroyedCallCount shouldBe 1
      }
    }

    context("FakePreviewSurfaceProvider functionality") {
      test("tracks all surface available calls") {
        provider.onSurfaceAvailable("surface1")
        provider.onSurfaceAvailable("surface2")

        provider.surfaceAvailableCalls shouldBe listOf("surface1", "surface2")
        provider.lastSurface shouldBe "surface2"
      }

      test("reset clears all state") {
        provider.onSurfaceAvailable("surface")
        provider.onSurfaceDestroyed()

        provider.reset()

        provider.lastSurface.shouldBeNull()
        provider.isDestroyed shouldBe false
        provider.onSurfaceAvailableCallCount shouldBe 0
        provider.onSurfaceDestroyedCallCount shouldBe 0
        provider.surfaceAvailableCalls shouldBe emptyList()
      }
    }
  })
