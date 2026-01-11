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

import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin

/**
 * DSL marker for CameraController configuration.
 * Prevents implicit receivers from being accessible in nested scopes.
 */
@DslMarker
annotation class CameraControllerDsl

/**
 * Scope class for configuring a CameraController using Kotlin DSL.
 *
 * Example usage:
 * ```kotlin
 * val controller = CameraController {
 *     configuration = CameraConfiguration(lens = CameraLens.BACK)
 *
 *     extensions {
 *         +ManualFocusExtension()
 *         +WhiteBalanceExtension()
 *     }
 *
 *     plugins {
 *         +QRScannerPlugin()
 *     }
 *
 *     imageCaptureUseCase = RawCaptureUseCase()
 * }
 * ```
 */
@CameraControllerDsl
class CameraControllerScope {
  /**
   * Camera configuration to apply.
   */
  var configuration: CameraConfiguration = CameraConfiguration()

  /**
   * Custom image capture use case. If null, uses default implementation.
   */
  var imageCaptureUseCase: ImageCaptureUseCase? = null

  /**
   * Custom video capture use case. If null, uses default implementation.
   */
  var videoCaptureUseCase: VideoCaptureUseCase? = null

  internal val _extensions = mutableListOf<CameraControlExtension>()
  internal val _plugins = mutableListOf<CameraPlugin>()

  /**
   * Configure extensions using DSL.
   *
   * Example:
   * ```kotlin
   * extensions {
   *     +ManualFocusExtension()
   *     +WhiteBalanceExtension()
   * }
   * ```
   */
  fun extensions(block: ExtensionsScope.() -> Unit) {
    ExtensionsScope(_extensions).apply(block)
  }

  /**
   * Configure plugins using DSL.
   *
   * Example:
   * ```kotlin
   * plugins {
   *     +QRScannerPlugin()
   *     +TextRecognitionPlugin()
   * }
   * ```
   */
  fun plugins(block: PluginsScope.() -> Unit) {
    PluginsScope(_plugins).apply(block)
  }

  /**
   * Build the CameraController from this scope's configuration.
   *
   * @return A configured CameraController
   */
  internal fun build(): CameraController {
    // Merge plugins from DSL with any plugins in configuration
    val mergedConfig = configuration.copy(
      plugins = configuration.plugins + _plugins,
    )

    // Create controller via factory
    val controller = createCameraControllerFactory().create(mergedConfig)

    // Register extensions
    _extensions.forEach { extension ->
      controller.registerExtension(extension)
    }

    // Set custom use cases if provided
    imageCaptureUseCase?.let { controller.imageCaptureUseCase = it }
    videoCaptureUseCase?.let { controller.videoCaptureUseCase = it }

    return controller
  }
}

/**
 * Scope for adding camera control extensions using the + operator.
 */
@CameraControllerDsl
class ExtensionsScope(private val list: MutableList<CameraControlExtension>) {
  /**
   * Add an extension using the unary plus operator.
   *
   * Example: `+ManualFocusExtension()`
   */
  operator fun CameraControlExtension.unaryPlus() {
    list.add(this)
  }

  /**
   * Add an extension explicitly.
   */
  fun add(extension: CameraControlExtension) {
    list.add(extension)
  }
}

/**
 * Scope for adding camera plugins using the + operator.
 */
@CameraControllerDsl
class PluginsScope(private val list: MutableList<CameraPlugin>) {
  /**
   * Add a plugin using the unary plus operator.
   *
   * Example: `+QRScannerPlugin()`
   */
  operator fun CameraPlugin.unaryPlus() {
    list.add(this)
  }

  /**
   * Add a plugin explicitly.
   */
  fun add(plugin: CameraPlugin) {
    list.add(plugin)
  }
}
