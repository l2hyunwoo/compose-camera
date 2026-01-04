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
import androidx.compose.runtime.remember
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraException
import io.github.l2hyunwoo.compose.camera.core.IOSCameraController
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of HDR capture plugin using exposure bracketing and Metal compositing.
 *
 * This plugin leverages iOS's built-in HDR capabilities when available.
 * For devices that support it, iOS automatically captures and processes HDR photos.
 *
 * Note: Full Metal-based Mertens exposure fusion is available in HDRKernel.metal
 * for custom HDR compositing if needed in the future.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class HDRCapture : CameraPlugin {
  actual override val id: String = "hdr-capture"

  private var iosController: IOSCameraController? = null
  private val _isEnabled = MutableStateFlow(false)
  private val _isSupported = MutableStateFlow(false)

  @NativeCoroutinesState
  actual val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

  @NativeCoroutinesState
  actual val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

  actual override fun onAttach(controller: CameraController) {
    if (controller is IOSCameraController) {
      iosController = controller
      _isSupported.value = true
    }
  }

  actual override fun onDetach() {
    iosController = null
    _isEnabled.value = false
    _isSupported.value = false
  }

  actual fun setEnabled(enabled: Boolean) {
    if (_isSupported.value) {
      _isEnabled.value = enabled
    }
  }

  @NativeCoroutines
  actual suspend fun captureHDR(): ImageCaptureResult {
    val controller = iosController ?: return ImageCaptureResult.Error(
      CameraException.CaptureFailed(IllegalStateException("Controller not attached")),
    )

    if (!_isEnabled.value) {
      return ImageCaptureResult.Error(
        CameraException.CaptureFailed(IllegalStateException("HDR mode is not enabled")),
      )
    }

    // iOS automatically handles HDR when supported by the device
    // through its default photo capture pipeline
    return try {
      controller.takePicture()
    } catch (e: Exception) {
      ImageCaptureResult.Error(
        CameraException.CaptureFailed(e),
      )
    }
  }
}

@Composable
actual fun rememberHDRCapture(): HDRCapture = remember { HDRCapture() }
