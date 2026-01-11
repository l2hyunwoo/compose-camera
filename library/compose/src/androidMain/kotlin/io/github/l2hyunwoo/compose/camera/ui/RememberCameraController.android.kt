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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraControllerContext
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraControllerScope

/**
 * Android implementation of [rememberCameraController].
 * Creates and remembers an [AndroidCameraController] instance.
 *
 * This implementation uses [LocalContext] and [LocalLifecycleOwner] to obtain
 * the Android-specific dependencies required by CameraX.
 *
 * Note: The controller is created once and configuration updates
 * should be applied via [CameraController.updateConfiguration].
 */
@Composable
actual fun rememberCameraController(
  configuration: CameraConfiguration,
): CameraController {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  return remember {
    AndroidCameraController(
      context = context,
      lifecycleOwner = lifecycleOwner,
      initialConfiguration = configuration,
    )
  }
}

/**
 * Android implementation of [rememberCameraController] with DSL configuration.
 * Creates and remembers an [AndroidCameraController] instance with extensions,
 * plugins, and custom use cases.
 *
 * This implementation uses [LocalContext] and [LocalLifecycleOwner] to obtain
 * the Android-specific dependencies required by CameraX, then applies the DSL
 * configuration block.
 */
@Composable
actual fun rememberCameraController(
  block: CameraControllerScope.() -> Unit,
): CameraController {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  return remember {
    // Initialize context for factory usage
    AndroidCameraControllerContext.initialize(context, lifecycleOwner)

    // Build controller using DSL scope
    CameraControllerScope().apply(block).build()
  }
}
