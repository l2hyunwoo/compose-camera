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

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.FocusPoint
import io.github.l2hyunwoo.compose.camera.core.initialize
import io.github.l2hyunwoo.compose.camera.core.rememberCameraController
import io.github.l2hyunwoo.compose.camera.core.surfaceRequestFlow
import kotlinx.coroutines.delay

/**
 * Android implementation of CameraPreview using CameraX Compose Viewfinder.
 */
@Composable
actual fun CameraPreview(
  modifier: Modifier,
  configuration: CameraConfiguration,
  onCameraControllerReady: (CameraController) -> Unit,
  focusIndicator: @Composable BoxScope.(tapPosition: Offset) -> Unit,
) {
  val controller = rememberCameraController(configuration)
  var tapPosition by remember { mutableStateOf(Offset.Unspecified) }

  // Initialize camera
  LaunchedEffect(controller) {
    controller.initialize()
    onCameraControllerReady(controller)
  }

  // Handle configuration updates
  LaunchedEffect(configuration) {
    controller.updateConfiguration(configuration)
  }

  // Auto-dismiss focus indicator
  LaunchedEffect(tapPosition) {
    if (tapPosition != Offset.Unspecified) {
      delay(2500)
      tapPosition = Offset.Unspecified
    }
  }

  val surfaceRequest by controller.surfaceRequestFlow.collectAsState()

  // Cleanup on dispose
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  // Render the camera preview with pinch zoom gesture
  Box(modifier = modifier) {
    surfaceRequest?.let { request ->
      CameraXViewfinder(
        surfaceRequest = request,
        modifier = Modifier
          .matchParentSize()
          .pointerInput(controller) {
            detectTransformGestures { _, _, zoom, _ ->
              val currentRatio = controller.cameraInfo.zoomState.value.zoomRatio
              val newZoomRatio = (currentRatio * zoom).coerceIn(
                controller.cameraInfo.zoomState.value.minZoomRatio,
                controller.cameraInfo.zoomState.value.maxZoomRatio,
              )
              controller.cameraControl.setZoom(newZoomRatio)
            }
          }
          .pointerInput(controller) {
            detectTapGestures { offset ->
              tapPosition = offset
              // Calculate normalized point (0..1)
              // NOTE: This assumes a CENTER_CROP / FILL_CENTER style preview where the camera
              // output fully fills the composable bounds without letterboxing or padding.
              // If the preview uses a different scale type or aspect ratio (e.g. is not full-screen
              // or has black bars), this simple normalization will NOT accurately map to the
              // camera sensor coordinates and tap-to-focus may target the wrong region.
              //
              // For configurations where accurate mapping is required regardless of aspect ratio
              // or scaling behavior, a full Matrix-based coordinate transformation such as
              // CameraX's CoordinateTransformer (preview <-> sensor coordinates) should be used.
              val viewWidth = size.width.toFloat()
              val viewHeight = size.height.toFloat()
              val focusPoint = FocusPoint.clamped(
                x = offset.x / viewWidth,
                y = offset.y / viewHeight,
              )
              controller.cameraControl.focus(focusPoint)
            }
          },
      )

      if (tapPosition != Offset.Unspecified) {
        focusIndicator(tapPosition)
      }
    }
  }
}
