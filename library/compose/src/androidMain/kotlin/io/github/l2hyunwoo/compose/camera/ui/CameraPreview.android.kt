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
import io.github.l2hyunwoo.compose.camera.core.initialize
import io.github.l2hyunwoo.compose.camera.core.rememberCameraController
import io.github.l2hyunwoo.compose.camera.core.surfaceRequestFlow

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
              val currentRatio = controller.zoomRatioFlow.value
              val newZoomRatio = (currentRatio * zoom).coerceIn(
                controller.minZoomRatio,
                controller.maxZoomRatio,
              )
              controller.setZoom(newZoomRatio)
            }
          }
          .pointerInput(controller) {
            detectTapGestures { offset ->
              tapPosition = offset
              // Calculate normalized point (0..1)
              // Assuming CENTER_CROP scaling (FILL_CENTER) which is typical for Viewfinder
              val viewWidth = size.width.toFloat()
              val viewHeight = size.height.toFloat()
              
              // Normalize coordinates assuming the surface fills the view
              // This acts as a simplified transformation which is sufficient for simple center-crop
              // For perfect accuracy given different aspect ratios, full Matrix transformation 
              // like CoordinateTransformer would be needed, but simple normalization usually works
              // well enough for tap-to-focus on full-screen type previews.
              val normalizedPoint = Offset(
                x = offset.x / viewWidth,
                y = offset.y / viewHeight
              )
              controller.focus(normalizedPoint)
            }
          },
      )
      
      if (tapPosition != Offset.Unspecified) {
        focusIndicator(tapPosition)
      }
    }
  }
}
