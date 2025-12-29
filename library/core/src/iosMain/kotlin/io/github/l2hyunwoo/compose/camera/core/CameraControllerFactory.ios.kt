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
import androidx.compose.runtime.remember
import platform.AVFoundation.AVCaptureSession

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
 * Initialize the iOS camera controller.
 */
actual suspend fun CameraController.initialize() {
  (this as IOSCameraController).initialize()
}

/**
 * Get the native AVCaptureSession for iOS preview.
 */
actual val CameraController.nativePreviewRequest: Any?
  get() = (this as IOSCameraController).captureSession

/**
 * Extension to get typed AVCaptureSession for iOS.
 */
val CameraController.captureSession: AVCaptureSession
  get() = (this as IOSCameraController).captureSession
