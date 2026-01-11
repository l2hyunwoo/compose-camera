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

import android.content.Context
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.StateFlow

/**
 * Android implementation of [CameraControllerFactory].
 * Requires Context and LifecycleOwner for CameraX integration.
 */
internal class AndroidCameraControllerFactory(
  private val context: Context,
  private val lifecycleOwner: LifecycleOwner,
) : CameraControllerFactory {

  override fun create(configuration: CameraConfiguration): CameraController = AndroidCameraController(
    context = context,
    lifecycleOwner = lifecycleOwner,
    initialConfiguration = configuration,
  )
}

/**
 * Holder for Android platform context required by factory.
 * Must be initialized before using CameraController() fake constructor.
 */
object AndroidCameraControllerContext {
  internal var context: Context? = null
  internal var lifecycleOwner: LifecycleOwner? = null

  /**
   * Initialize the Android camera controller context.
   * Call this before using CameraController() outside of Compose.
   *
   * @param context Application or Activity context
   * @param lifecycleOwner Lifecycle owner for camera binding
   */
  fun initialize(context: Context, lifecycleOwner: LifecycleOwner) {
    this.context = context.applicationContext
    this.lifecycleOwner = lifecycleOwner
  }

  /**
   * Clear the context. Call when lifecycle owner is destroyed.
   */
  fun clear() {
    this.context = null
    this.lifecycleOwner = null
  }
}

/**
 * Get the Android [CameraControllerFactory].
 * Requires [AndroidCameraControllerContext] to be initialized.
 *
 * @throws IllegalStateException if context is not initialized
 */
actual fun createCameraControllerFactory(): CameraControllerFactory {
  val context = AndroidCameraControllerContext.context
    ?: error("AndroidCameraControllerContext.initialize() must be called before using CameraController()")
  val lifecycleOwner = AndroidCameraControllerContext.lifecycleOwner
    ?: error("AndroidCameraControllerContext.initialize() must be called before using CameraController()")

  return AndroidCameraControllerFactory(context, lifecycleOwner)
}

/**
 * Android implementation of [rememberCameraController].
 * Creates and remembers an [AndroidCameraController] instance.
 *
 * Note: The controller is created once and configuration updates
 * should be applied via [CameraController.updateConfiguration].
 *
 * @deprecated Use rememberCameraController from compose module instead.
 */
@Deprecated(
  message = "Use rememberCameraController from io.github.l2hyunwoo.compose.camera.ui package instead",
  replaceWith = ReplaceWith(
    "rememberCameraController(configuration)",
    "io.github.l2hyunwoo.compose.camera.ui.rememberCameraController",
  ),
  level = DeprecationLevel.WARNING,
)
@Composable
actual fun rememberCameraController(
  configuration: CameraConfiguration,
): CameraController {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  return remember {
    AndroidCameraController(
      context = context,
      lifecycleOwner = lifecycleOwner,
      initialConfiguration = configuration,
    )
  }
}

/**
 * Initialize the Android camera controller.
 */
actual suspend fun CameraController.initialize() {
  (this as AndroidCameraController).initialize()
}

/**
 * Get the native SurfaceRequest for CameraX Viewfinder.
 */
actual val CameraController.nativePreviewRequest: Any?
  get() = (this as AndroidCameraController).surfaceRequest.value

/**
 * Extension to get typed SurfaceRequest StateFlow for Android.
 */
val CameraController.surfaceRequestFlow: StateFlow<SurfaceRequest?>
  get() = (this as AndroidCameraController).surfaceRequest
