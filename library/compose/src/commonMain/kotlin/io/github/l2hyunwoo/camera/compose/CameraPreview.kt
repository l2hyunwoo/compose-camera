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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Camera preview composable.
 * Displays the camera preview and provides a [CameraController] for camera operations.
 *
 * Example:
 * ```
 * var controller by remember { mutableStateOf<CameraController?>(null) }
 *
 * CameraPreview(
 *     modifier = Modifier.fillMaxSize(),
 *     configuration = CameraConfiguration(
 *         lens = CameraLens.BACK,
 *         flashMode = FlashMode.OFF
 *     ),
 *     onCameraControllerReady = { controller = it }
 * )
 *
 * // Use controller to take pictures, record video, etc.
 * controller?.takePicture()
 * ```
 *
 * @param modifier Modifier for the preview
 * @param configuration Camera configuration
 * @param onCameraControllerReady Callback invoked when the camera controller is ready
 */
@Composable
expect fun CameraPreview(
  modifier: Modifier = Modifier,
  configuration: CameraConfiguration = CameraConfiguration(),
  onCameraControllerReady: (CameraController) -> Unit = {},
)
