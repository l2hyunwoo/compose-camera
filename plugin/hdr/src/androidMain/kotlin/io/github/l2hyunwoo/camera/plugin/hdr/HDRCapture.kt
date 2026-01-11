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

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraException
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import io.github.l2hyunwoo.compose.camera.core.plugin.ExtensionProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

/**
 * Android implementation of HDR capture plugin using CameraX Extensions API.
 *
 * This plugin uses OEM-provided HDR processing through CameraX Extensions.
 * When HDR mode is enabled, the camera is rebound with an HDR-enabled CameraSelector.
 *
 * @param context Android context for initializing CameraX Extensions
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class HDRCapture(
  private val context: Context,
) : CameraPlugin,
  ExtensionProvider {
  actual override val id: String = "hdr-capture"

  private var extensionsManager: ExtensionsManager? = null
  private var cameraProvider: ProcessCameraProvider? = null
  private var androidController: AndroidCameraController? = null
  private val _isEnabled = MutableStateFlow(false)
  private val _isSupported = MutableStateFlow(false)
  private var scopeJob: Job? = null
  private var scope: CoroutineScope? = null

  @NativeCoroutinesState
  actual val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

  @NativeCoroutinesState
  actual val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

  actual override fun onAttach(controller: CameraController) {
    if (controller is AndroidCameraController) {
      androidController = controller
      // Create a new scope for each attachment to handle re-attachment scenarios
      scopeJob = SupervisorJob()
      scope = CoroutineScope(Dispatchers.Main + scopeJob!!)
      // Initialize ExtensionsManager using coroutines
      scope?.launch {
        initializeExtensions()
      }
    }
  }

  actual override fun onDetach() {
    scopeJob?.cancel()
    scopeJob = null
    scope = null
    extensionsManager = null
    cameraProvider = null
    androidController = null
    _isEnabled.value = false
  }

  private suspend fun initializeExtensions() {
    try {
      // await() from kotlinx-coroutines-guava
      val provider = ProcessCameraProvider.getInstance(context).await()
      cameraProvider = provider

      val manager = ExtensionsManager.getInstanceAsync(context, provider).await()
      extensionsManager = manager

      // Check HDR support for both back and front cameras
      _isSupported.value = manager.isExtensionAvailable(
        CameraSelector.DEFAULT_BACK_CAMERA,
        ExtensionMode.HDR,
      ) || manager.isExtensionAvailable(
        CameraSelector.DEFAULT_FRONT_CAMERA,
        ExtensionMode.HDR,
      )
    } catch (e: CancellationException) {
      throw e // Re-throw for structured concurrency
    } catch (e: Exception) {
      Log.e(TAG, "Failed to initialize HDR extensions", e)
      _isSupported.value = false
    }
  }

  actual fun setEnabled(enabled: Boolean) {
    if (_isSupported.value && enabled != _isEnabled.value) {
      // Update state BEFORE rebind so getExtensionCameraSelector() reads correct value
      val previousState = _isEnabled.value
      _isEnabled.value = enabled

      // Attempt to rebind camera with new HDR state
      val rebindSuccess = androidController?.rebindCamera() ?: false
      if (!rebindSuccess) {
        // Rebind was skipped (e.g., during active recording), revert state
        _isEnabled.value = previousState
        Log.w(TAG, "HDR state change skipped: camera rebind not possible at this time")
      }
    }
  }

  @NativeCoroutines
  actual suspend fun captureHDR(): ImageCaptureResult {
    val controller = androidController ?: return ImageCaptureResult.Error(
      CameraException.CaptureFailed(IllegalStateException("Controller not attached")),
    )

    if (!_isEnabled.value) {
      return ImageCaptureResult.Error(
        CameraException.CaptureFailed(IllegalStateException("HDR mode is not enabled")),
      )
    }

    // When HDR mode is enabled, the standard takePicture() already uses HDR processing
    return controller.takePicture()
  }

  /**
   * Get an HDR-enabled CameraSelector for the specified base selector.
   *
   * @param baseSelector The base camera selector (front or back)
   * @return HDR-enabled CameraSelector, or null if HDR is not available
   */
  override fun getExtensionCameraSelector(baseSelector: CameraSelector): CameraSelector? {
    if (!_isEnabled.value) return null
    val manager = extensionsManager ?: return null

    return if (manager.isExtensionAvailable(baseSelector, ExtensionMode.HDR)) {
      manager.getExtensionEnabledCameraSelector(baseSelector, ExtensionMode.HDR)
    } else {
      null
    }
  }

  /**
   * Check if HDR is available for the specified camera.
   *
   * @param baseSelector The camera selector to check
   * @return true if HDR is available
   */
  fun isHDRAvailable(baseSelector: CameraSelector): Boolean = extensionsManager?.isExtensionAvailable(baseSelector, ExtensionMode.HDR) ?: false

  private companion object {
    private const val TAG = "HDRCapture"
  }
}

@Composable
actual fun rememberHDRCapture(): HDRCapture {
  val context = LocalContext.current
  return remember(context) { HDRCapture(context) }
}
