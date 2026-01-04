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
package io.github.l2hyunwoo.camera.plugin.hdr

import androidx.compose.runtime.Composable
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import kotlinx.coroutines.flow.StateFlow

/**
 * HDR (High Dynamic Range) capture plugin.
 *
 * This plugin provides HDR capture functionality:
 * - Android: Uses CameraX Extensions API for automatic OEM-based HDR processing.
 *   Create with: `HDRCapture(context)`
 * - iOS: Uses exposure bracketing with Metal-based HDR compositing.
 *   Create with: `HDRCapture()`
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class HDRCapture : CameraPlugin {
  /**
   * Unique identifier for this plugin
   */
  override val id: String

  /**
   * Called when the plugin is attached to a camera controller
   */
  override fun onAttach(controller: CameraController)

  /**
   * Called when the plugin is detached from the camera controller
   */
  override fun onDetach()

  /**
   * Whether HDR capture is supported on the current device.
   * On Android, this depends on OEM Extensions support.
   * On iOS, this depends on bracketed capture support.
   */
  @NativeCoroutinesState
  val isSupported: StateFlow<Boolean>

  /**
   * Current HDR mode enabled state.
   */
  @NativeCoroutinesState
  val isEnabled: StateFlow<Boolean>

  /**
   * Enable or disable HDR mode.
   * When enabled on Android, the camera will be rebound with HDR extension.
   *
   * @param enabled true to enable HDR mode
   */
  fun setEnabled(enabled: Boolean)

  /**
   * Capture an HDR photo.
   *
   * On Android, this uses the standard capture with HDR extension enabled.
   * On iOS, this captures bracketed photos and composites them using Metal.
   *
   * @return The result of the HDR capture operation
   */
  @NativeCoroutines
  suspend fun captureHDR(): ImageCaptureResult
}

/**
 * Creates and remembers an instance of [HDRCapture].
 *
 * @return Remembered [HDRCapture] instance
 */
@Composable
expect fun rememberHDRCapture(): HDRCapture
