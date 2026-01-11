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

/**
 * Interface for providing a preview surface to the camera controller.
 * This allows custom preview implementations without depending on the compose module.
 *
 * Platform-specific interfaces extend this with typed access:
 * - Android: [AndroidPreviewSurfaceProvider] with SurfaceRequest support
 * - iOS: [IOSPreviewSurfaceProvider] with AVCaptureSession access
 *
 * Example usage (Android):
 * ```kotlin
 * class CustomPreviewView(context: Context) : SurfaceView(context) {
 *     private val provider = object : AndroidPreviewSurfaceProvider {
 *         override fun onSurfaceRequestAvailable(request: SurfaceRequest) {
 *             request.provideSurface(holder.surface, executor) { result -> }
 *         }
 *         override fun onSurfaceAvailable(surface: Any) {}
 *         override fun onSurfaceDestroyed() {}
 *     }
 *
 *     fun attach(controller: CameraController) {
 *         controller.setPreviewSurfaceProvider(provider)
 *     }
 * }
 * ```
 */
interface PreviewSurfaceProvider {
  /**
   * Called when a preview surface becomes available.
   * @param surface Platform-specific surface object
   */
  fun onSurfaceAvailable(surface: Any)

  /**
   * Called when the preview surface is destroyed.
   */
  fun onSurfaceDestroyed()
}
