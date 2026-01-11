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

import androidx.compose.runtime.Composable

/**
 * Factory interface for creating platform-specific [CameraController] instances.
 * This abstracts the platform-specific controller creation.
 */
interface CameraControllerFactory {
  /**
   * Create a new CameraController with the given configuration.
   *
   * @param configuration The camera configuration to apply
   * @return A platform-specific CameraController implementation
   */
  fun create(configuration: CameraConfiguration): CameraController
}

/**
 * Get the platform-specific [CameraControllerFactory] implementation.
 */
expect fun createCameraControllerFactory(): CameraControllerFactory

/**
 * Fake Constructor - Create a CameraController with default configuration.
 *
 * This follows the Kotlin "Fake Constructor" pattern - a function named like
 * a class that provides convenient instantiation.
 *
 * Example:
 * ```kotlin
 * val controller = CameraController()
 * controller.initialize()
 * ```
 *
 * @return A platform-specific CameraController with default configuration
 */
fun CameraController(): CameraController = createCameraControllerFactory().create(CameraConfiguration())

/**
 * Fake Constructor with DSL - Create a CameraController with custom configuration.
 *
 * This follows the Kotlin "Fake Constructor" pattern combined with DSL for
 * a clean, readable configuration syntax.
 *
 * Example:
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
 *
 * @param block DSL block for configuring the controller
 * @return A configured CameraController
 */
fun CameraController(block: CameraControllerScope.() -> Unit): CameraController {
  val scope = CameraControllerScope().apply(block)
  return scope.build()
}

/**
 * Factory function to create a platform-specific [CameraController].
 *
 * This is the recommended way to create a CameraController as it abstracts
 * the platform-specific implementation details.
 *
 * @param configuration Initial camera configuration
 * @return A platform-specific CameraController implementation
 */
@Composable
expect fun rememberCameraController(
  configuration: CameraConfiguration = CameraConfiguration(),
): CameraController

/**
 * Initialize the camera controller.
 * Must be called before using other camera operations.
 */
expect suspend fun CameraController.initialize()

/**
 * Get the native preview request object.
 * - Android: SurfaceRequest from CameraX
 * - iOS: AVCaptureSession
 */
expect val CameraController.nativePreviewRequest: Any?
