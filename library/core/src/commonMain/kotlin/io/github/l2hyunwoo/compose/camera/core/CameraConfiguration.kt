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

import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin

/**
 * Immutable camera configuration.
 * Use [copy] to modify settings.
 *
 * Example:
 * ```
 * val config = CameraConfiguration()
 *     .copy(lens = CameraLens.FRONT, flashMode = FlashMode.ON)
 *     .withPlugin(qrScannerPlugin)
 * ```
 */
data class CameraConfiguration(
  val lens: CameraLens = CameraLens.BACK,
  val flashMode: FlashMode = FlashMode.OFF,
  val imageFormat: ImageFormat = ImageFormat.JPEG,
  val videoQuality: VideoQuality = VideoQuality.FHD,
  val targetFps: Int = 30,
  val enableHdr: Boolean = false,
  val directory: Directory = Directory.PICTURES,
  val captureMode: CaptureMode = CaptureMode.BALANCED,
  val photoResolution: Resolution? = null,
  val plugins: List<CameraPlugin> = emptyList(),
) {
  /**
   * Add a plugin to the configuration
   */
  fun withPlugin(plugin: CameraPlugin): CameraConfiguration = copy(plugins = plugins + plugin)

  /**
   * Add multiple plugins to the configuration
   */
  fun withPlugins(vararg pluginsToAdd: CameraPlugin): CameraConfiguration = copy(plugins = plugins + pluginsToAdd.toList())

  /**
   * Remove a plugin from the configuration
   */
  fun withoutPlugin(pluginId: String): CameraConfiguration = copy(plugins = plugins.filter { it.id != pluginId })
}
