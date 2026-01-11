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
import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraPlugin
import io.github.l2hyunwoo.compose.camera.core.fake.FakeImageCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.fake.FakeVideoCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class CameraControllerDslTest :
  FunSpec({

    context("CameraControllerScope Tests") {
      test("default scope has default configuration") {
        val scope = CameraControllerScope()

        scope.configuration shouldBe CameraConfiguration()
        scope.imageCaptureUseCase shouldBe null
        scope.videoCaptureUseCase shouldBe null
        scope._extensions shouldHaveSize 0
        scope._plugins shouldHaveSize 0
      }

      test("scope accepts empty block") {
        val scope = CameraControllerScope().apply { }

        scope.configuration shouldBe CameraConfiguration()
      }

      test("scope applies configuration") {
        val customConfig = CameraConfiguration(
          lens = CameraLens.FRONT,
          flashMode = FlashMode.AUTO,
        )

        val scope = CameraControllerScope().apply {
          configuration = customConfig
        }

        scope.configuration shouldBe customConfig
        scope.configuration.lens shouldBe CameraLens.FRONT
        scope.configuration.flashMode shouldBe FlashMode.AUTO
      }
    }

    context("Extensions DSL Tests") {
      test("extensions block with unary plus registers extension") {
        val extension = FakeCameraControlExtension(id = "test-ext")

        val scope = CameraControllerScope().apply {
          extensions {
            +extension
          }
        }

        scope._extensions shouldHaveSize 1
        scope._extensions shouldContain extension
      }

      test("multiple unary plus operators register all extensions") {
        val ext1 = FakeCameraControlExtension(id = "ext-1")
        val ext2 = FakeCameraControlExtension(id = "ext-2")
        val ext3 = FakeCameraControlExtension(id = "ext-3")

        val scope = CameraControllerScope().apply {
          extensions {
            +ext1
            +ext2
            +ext3
          }
        }

        scope._extensions shouldHaveSize 3
        scope._extensions shouldContain ext1
        scope._extensions shouldContain ext2
        scope._extensions shouldContain ext3
      }

      test("extensions can be added with add method") {
        val extension = FakeCameraControlExtension(id = "test-ext")

        val scope = CameraControllerScope().apply {
          extensions {
            add(extension)
          }
        }

        scope._extensions shouldHaveSize 1
        scope._extensions shouldContain extension
      }
    }

    context("Plugins DSL Tests") {
      test("plugins block with unary plus registers plugin") {
        val plugin = FakeCameraPlugin(id = "test-plugin")

        val scope = CameraControllerScope().apply {
          plugins {
            +plugin
          }
        }

        scope._plugins shouldHaveSize 1
        scope._plugins shouldContain plugin
      }

      test("multiple plugins can be registered") {
        val plugin1 = FakeCameraPlugin(id = "plugin-1")
        val plugin2 = FakeCameraPlugin(id = "plugin-2")

        val scope = CameraControllerScope().apply {
          plugins {
            +plugin1
            +plugin2
          }
        }

        scope._plugins shouldHaveSize 2
        scope._plugins shouldContain plugin1
        scope._plugins shouldContain plugin2
      }
    }

    context("UseCase assignment in DSL") {
      test("imageCaptureUseCase can be assigned") {
        val customUseCase = FakeImageCaptureUseCase()

        val scope = CameraControllerScope().apply {
          imageCaptureUseCase = customUseCase
        }

        scope.imageCaptureUseCase shouldBe customUseCase
      }

      test("videoCaptureUseCase can be assigned") {
        val customUseCase = FakeVideoCaptureUseCase()

        val scope = CameraControllerScope().apply {
          videoCaptureUseCase = customUseCase
        }

        scope.videoCaptureUseCase shouldBe customUseCase
      }
    }

    context("Complete DSL usage") {
      test("full DSL configuration works correctly") {
        val ext1 = FakeCameraControlExtension(id = "ext-1")
        val ext2 = FakeCameraControlExtension(id = "ext-2")
        val plugin1 = FakeCameraPlugin(id = "plugin-1")
        val imageUseCase = FakeImageCaptureUseCase()
        val videoUseCase = FakeVideoCaptureUseCase()

        val scope = CameraControllerScope().apply {
          configuration = CameraConfiguration(
            lens = CameraLens.BACK,
            flashMode = FlashMode.AUTO,
            videoQuality = VideoQuality.UHD,
          )

          extensions {
            +ext1
            +ext2
          }

          plugins {
            +plugin1
          }

          imageCaptureUseCase = imageUseCase
          videoCaptureUseCase = videoUseCase
        }

        // Verify all configurations
        scope.configuration.lens shouldBe CameraLens.BACK
        scope.configuration.flashMode shouldBe FlashMode.AUTO
        scope.configuration.videoQuality shouldBe VideoQuality.UHD
        scope._extensions shouldHaveSize 2
        scope._plugins shouldHaveSize 1
        scope.imageCaptureUseCase shouldBe imageUseCase
        scope.videoCaptureUseCase shouldBe videoUseCase
      }
    }

    context("ExtensionsScope isolation") {
      test("ExtensionsScope only exposes extension operations") {
        val extensions = mutableListOf<CameraControlExtension>()
        val scope = ExtensionsScope(extensions)
        val ext = FakeCameraControlExtension(id = "test")

        // Can use unary plus
        with(scope) {
          +ext
        }

        extensions shouldHaveSize 1

        // Can use add
        val ext2 = FakeCameraControlExtension(id = "test-2")
        scope.add(ext2)

        extensions shouldHaveSize 2
      }
    }

    context("PluginsScope isolation") {
      test("PluginsScope only exposes plugin operations") {
        val plugins = mutableListOf<CameraPlugin>()
        val scope = PluginsScope(plugins)
        val plugin = FakeCameraPlugin(id = "test")

        // Can use unary plus
        with(scope) {
          +plugin
        }

        plugins shouldHaveSize 1

        // Can use add
        val plugin2 = FakeCameraPlugin(id = "test-2")
        scope.add(plugin2)

        plugins shouldHaveSize 2
      }
    }
  })
