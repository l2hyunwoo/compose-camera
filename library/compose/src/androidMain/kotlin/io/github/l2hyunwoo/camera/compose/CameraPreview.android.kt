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
package io.github.l2hyunwoo.camera.compose

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.camera.core.CameraConfiguration
import io.github.l2hyunwoo.camera.core.CameraController
import io.github.l2hyunwoo.camera.core.initialize
import io.github.l2hyunwoo.camera.core.rememberCameraController
import io.github.l2hyunwoo.camera.core.surfaceRequestFlow

/**
 * Android implementation of CameraPreview using CameraX Compose Viewfinder.
 */
@Composable
actual fun CameraPreview(
  modifier: Modifier,
  configuration: CameraConfiguration,
  onCameraControllerReady: (CameraController) -> Unit,
) {
  val controller = rememberCameraController(configuration)

  // Initialize camera
  LaunchedEffect(controller) {
    controller.initialize()
    onCameraControllerReady(controller)
  }

  val surfaceRequest by controller.surfaceRequestFlow.collectAsState()

  // Cleanup on dispose
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  // Render the camera preview
  Box(modifier = modifier) {
    surfaceRequest?.let { request ->
      CameraXViewfinder(
        surfaceRequest = request,
        modifier = Modifier.matchParentSize(),
      )
    }
  }
}
