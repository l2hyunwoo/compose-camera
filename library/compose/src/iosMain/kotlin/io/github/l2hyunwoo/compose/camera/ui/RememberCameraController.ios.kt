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
package io.github.l2hyunwoo.compose.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraControllerScope
import io.github.l2hyunwoo.compose.camera.core.IOSCameraController

/**
 * iOS implementation of [rememberCameraController].
 * Creates and remembers an [IOSCameraController] instance.
 *
 * Note: The controller is created once and configuration updates
 * should be applied via [CameraController.updateConfiguration].
 */
@Composable
actual fun rememberCameraController(
  configuration: CameraConfiguration,
): CameraController = remember {
  IOSCameraController(initialConfiguration = configuration)
}

/**
 * iOS implementation of [rememberCameraController] with DSL configuration.
 * Creates and remembers an [IOSCameraController] instance with extensions,
 * plugins, and custom use cases.
 */
@Composable
actual fun rememberCameraController(
  block: CameraControllerScope.() -> Unit,
): CameraController = remember {
  CameraControllerScope().apply(block).build()
}
