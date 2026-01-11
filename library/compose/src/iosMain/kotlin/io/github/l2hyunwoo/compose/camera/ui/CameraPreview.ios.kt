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
@file:OptIn(ExperimentalForeignApi::class)

package io.github.l2hyunwoo.compose.camera.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.interop.UIKitView
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.FocusPoint
import io.github.l2hyunwoo.compose.camera.core.captureSession
import io.github.l2hyunwoo.compose.camera.core.initialize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UIPinchGestureRecognizer
import platform.UIKit.UIView

/**
 * iOS implementation of CameraPreview using AVCaptureVideoPreviewLayer.
 */
@OptIn(ExperimentalForeignApi::class)
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

  // Cleanup on dispose
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  Box(modifier = modifier) {
    // Create UIKit view with preview layer
    UIKitView(
      modifier = Modifier.matchParentSize(),
      factory = {
        val cameraView = CameraView(
          captureSession = controller.captureSession,
          onZoomChange = { scale, isStarting ->
            if (!isStarting) {
              val newZoom = (controller.cameraInfo.zoomState.value.zoomRatio * scale).coerceIn(
                controller.cameraInfo.zoomState.value.minZoomRatio,
                controller.cameraInfo.zoomState.value.maxZoomRatio,
              )
              controller.cameraControl.setZoom(newZoom)
            }
          },
          onTap = { offset, focusPoint ->
            tapPosition = offset
            controller.cameraControl.focus(focusPoint)
          },
        )
        cameraView
      },
    )

    if (tapPosition != Offset.Unspecified) {
      focusIndicator(tapPosition)
    }
  }
}

/**
 * Custom UIView that handles layout updates for the preview layer
 */
@OptIn(ExperimentalForeignApi::class)
private class CameraView(
  captureSession: AVCaptureSession,
  private val onZoomChange: (Float, Boolean) -> Unit,
  private val onTap: (Offset, FocusPoint) -> Unit,
) : UIView(frame = cValue { CGRectZero }) {

  private val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
    videoGravity = AVLayerVideoGravityResizeAspectFill
  }

  private var baseZoomScale: Float = 1.0f

  init {
    layer.addSublayer(previewLayer)

    // Add pinch gesture recognizer for zoom
    val pinchGesture = UIPinchGestureRecognizer(
      target = this,
      action = platform.objc.sel_registerName("handlePinch:"),
    )
    addGestureRecognizer(pinchGesture)

    // Add tap gesture recognizer for focus
    val tapGesture = platform.UIKit.UITapGestureRecognizer(
      target = this,
      action = platform.objc.sel_registerName("handleTap:"),
    )
    addGestureRecognizer(tapGesture)
  }

  @Suppress("unused")
  @kotlinx.cinterop.ObjCAction
  fun handlePinch(gesture: UIPinchGestureRecognizer) {
    when (gesture.state) {
      UIGestureRecognizerStateBegan -> {
        baseZoomScale = 1.0f
        onZoomChange(gesture.scale().toFloat(), true)
      }

      UIGestureRecognizerStateChanged -> {
        val scaleDelta = gesture.scale().toFloat() / baseZoomScale
        baseZoomScale = gesture.scale().toFloat()
        onZoomChange(scaleDelta, false)
      }
    }
  }

  @Suppress("unused")
  @kotlinx.cinterop.ObjCAction
  fun handleTap(gesture: platform.UIKit.UITapGestureRecognizer) {
    val location = gesture.locationInView(this)
    val point = previewLayer.captureDevicePointOfInterestForPoint(location)
    val focusPoint = FocusPoint.clamped(
      point.useContents { x }.toFloat(),
      point.useContents { y }.toFloat(),
    )

    val tapPoint = Offset(location.useContents { x }.toFloat(), location.useContents { y }.toFloat())
    onTap(tapPoint, focusPoint)
  }

  override fun layoutSubviews() {
    super.layoutSubviews()
    CATransaction.begin()
    CATransaction.setDisableActions(true)
    previewLayer.frame = bounds
    CATransaction.commit()
  }
}
