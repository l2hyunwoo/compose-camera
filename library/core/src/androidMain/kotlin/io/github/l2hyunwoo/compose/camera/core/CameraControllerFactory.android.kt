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

import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.flow.StateFlow

/**
 * Android implementation of [rememberCameraController].
 * Creates and remembers an [AndroidCameraController] instance.
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
 * Initialize the Android camera controller.
 */
actual suspend fun CameraController.initialize() {
  (this as AndroidCameraController).initialize()
}

/**
 * Get the native SurfaceRequest for CameraX Viewfinder.
 */
actual val CameraController.nativePreviewRequest: Any?
  get() = (this as AndroidCameraController).surfaceRequest.value

/**
 * Extension to get typed SurfaceRequest StateFlow for Android.
 */
val CameraController.surfaceRequestFlow: StateFlow<SurfaceRequest?>
  get() = (this as AndroidCameraController).surfaceRequest
