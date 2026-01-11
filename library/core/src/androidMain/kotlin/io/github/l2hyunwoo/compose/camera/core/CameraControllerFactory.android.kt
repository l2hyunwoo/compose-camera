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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

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
 *
 * Note: LifecycleOwner is held via WeakReference and automatically cleared
 * when the lifecycle is destroyed to prevent Activity leaks.
 */
object AndroidCameraControllerContext {
  internal var context: Context? = null
  private var lifecycleOwnerRef: WeakReference<LifecycleOwner>? = null
  private var lifecycleObserver: LifecycleEventObserver? = null

  internal val lifecycleOwner: LifecycleOwner?
    get() = lifecycleOwnerRef?.get()

  /**
   * Initialize the Android camera controller context.
   * Call this before using CameraController() outside of Compose.
   *
   * The lifecycleOwner is held weakly and automatically cleared when destroyed
   * to prevent Activity memory leaks.
   *
   * @param context Application or Activity context
   * @param lifecycleOwner Lifecycle owner for camera binding
   */
  fun initialize(context: Context, lifecycleOwner: LifecycleOwner) {
    // Remove observer from previous lifecycle owner if any
    clearLifecycleObserver()

    this.context = context.applicationContext
    this.lifecycleOwnerRef = WeakReference(lifecycleOwner)

    // Register observer to auto-clear when lifecycle is destroyed
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_DESTROY) {
        clear()
      }
    }
    this.lifecycleObserver = observer
    lifecycleOwner.lifecycle.addObserver(observer)
  }

  /**
   * Clear the context. Called automatically when lifecycle owner is destroyed.
   */
  fun clear() {
    clearLifecycleObserver()
    this.context = null
    this.lifecycleOwnerRef = null
  }

  private fun clearLifecycleObserver() {
    lifecycleObserver?.let { observer ->
      lifecycleOwnerRef?.get()?.lifecycle?.removeObserver(observer)
    }
    lifecycleObserver = null
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
 * Extension to get typed SurfaceRequest StateFlow for Android.
 */
val CameraController.surfaceRequestFlow: StateFlow<SurfaceRequest?>
  get() = (this as AndroidCameraController).surfaceRequest
