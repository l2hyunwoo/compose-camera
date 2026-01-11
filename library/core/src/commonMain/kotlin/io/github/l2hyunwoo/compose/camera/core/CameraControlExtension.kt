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
 * Interface for extending camera control capabilities.
 * Extensions can add custom controls (e.g., manual focus distance, white balance)
 * without modifying the core CameraControl interface.
 *
 * Example implementation:
 * ```kotlin
 * class ManualFocusExtension : CameraControlExtension {
 *     override val id = "manual-focus"
 *     private var controller: CameraController? = null
 *
 *     var focusDistance: Float = 0f
 *         set(value) {
 *             field = value
 *             applyFocusDistance(value)
 *         }
 *
 *     override fun onAttach(controller: CameraController) {
 *         this.controller = controller
 *     }
 *
 *     override fun onDetach() {
 *         controller = null
 *     }
 * }
 * ```
 */
interface CameraControlExtension {
  /**
   * Unique identifier for this extension.
   * Used for registration and retrieval.
   */
  val id: String

  /**
   * Called when the extension is attached to a camera controller.
   * @param controller The controller this extension is attached to
   */
  fun onAttach(controller: CameraController)

  /**
   * Called when the extension is detached from a camera controller.
   */
  fun onDetach()

  /**
   * Called when the camera becomes ready (after initialization).
   * This is the safe point to access camera hardware features.
   */
  fun onCameraReady() {}

  /**
   * Called before the camera is released.
   * Clean up any camera-related resources here.
   */
  fun onCameraReleased() {}
}
