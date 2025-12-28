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
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.l2hyunwoo.camera.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.QuartzCore.CATransaction
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
  // Create and remember the camera controller
  val controller = remember(configuration) {
    IOSCameraController(initialConfiguration = configuration)
  }

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

  // Create UIKit view with preview layer
  UIKitView(
    modifier = modifier,
    factory = {
      val cameraView = CameraView(controller.captureSession)
      cameraView
    },
  )
}

/**
 * Custom UIView that handles layout updates for the preview layer
 */
@OptIn(ExperimentalForeignApi::class)
private class CameraView(
  private val captureSession: platform.AVFoundation.AVCaptureSession,
) : UIView(frame = kotlinx.cinterop.cValue { platform.CoreGraphics.CGRectZero }) {

  private val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
    videoGravity = AVLayerVideoGravityResizeAspectFill
  }

  init {
    layer.addSublayer(previewLayer)
  }

  override fun layoutSubviews() {
    super.layoutSubviews()
    CATransaction.begin()
    CATransaction.setDisableActions(true)
    previewLayer.frame = bounds
    CATransaction.commit()
  }
}
