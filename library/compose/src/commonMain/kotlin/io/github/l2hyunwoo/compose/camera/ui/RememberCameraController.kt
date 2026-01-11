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
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController

/**
 * Creates and remembers a [CameraController] instance for use in Compose.
 *
 * This is the recommended way to create a CameraController in Compose applications
 * as it properly integrates with the Compose lifecycle.
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun CameraScreen() {
 *     val controller = rememberCameraController(
 *         configuration = CameraConfiguration(lens = CameraLens.BACK)
 *     )
 *
 *     LaunchedEffect(controller) {
 *         controller.initialize()
 *     }
 *
 *     CameraPreview(
 *         controller = controller,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 *
 * @param configuration Initial camera configuration
 * @return A remembered CameraController instance
 */
@Composable
expect fun rememberCameraController(
  configuration: CameraConfiguration = CameraConfiguration(),
): CameraController
