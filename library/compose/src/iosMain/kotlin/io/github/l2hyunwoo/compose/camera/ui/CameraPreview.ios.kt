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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.captureSession
import io.github.l2hyunwoo.compose.camera.core.initialize
import io.github.l2hyunwoo.compose.camera.core.rememberCameraController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
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
) {
  val controller = rememberCameraController(configuration)

  // Initialize camera
  LaunchedEffect(controller) {
    controller.initialize()
    onCameraControllerReady(controller)
  }

  // Cleanup on dispose
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  // Create UIKit view with preview layer using extension
  UIKitView(
    modifier = modifier,
    factory = {
      val cameraView = CameraView(
        captureSession = controller.captureSession,
        onZoomChange = { scale, isStarting ->
          if (!isStarting) {
            val newZoom = (controller.zoomRatioFlow.value * scale).coerceIn(
              controller.minZoomRatio,
              controller.maxZoomRatio,
            )
            controller.setZoom(newZoom)
          }
        },
      )
      cameraView
    },
  )
}

/**
 * Custom UIView that handles layout updates for the preview layer
 */
@OptIn(ExperimentalForeignApi::class)
private class CameraView(
  captureSession: AVCaptureSession,
  private val onZoomChange: (Float, Boolean) -> Unit,
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

  override fun layoutSubviews() {
    super.layoutSubviews()
    CATransaction.begin()
    CATransaction.setDisableActions(true)
    previewLayer.frame = bounds
    CATransaction.commit()
  }
}
