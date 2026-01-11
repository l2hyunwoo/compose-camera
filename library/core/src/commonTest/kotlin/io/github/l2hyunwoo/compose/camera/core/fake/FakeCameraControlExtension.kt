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
package io.github.l2hyunwoo.compose.camera.core.fake

import io.github.l2hyunwoo.compose.camera.core.CameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.CameraController

/**
 * Fake implementation of [CameraControlExtension] for testing.
 * Records all lifecycle calls for verification.
 */
class FakeCameraControlExtension(
  override val id: String = "fake-extension",
) : CameraControlExtension {

  /**
   * The controller this extension is attached to, or null if not attached.
   */
  var attachedController: CameraController? = null
    private set

  /**
   * Whether the extension is currently attached.
   */
  val isAttached: Boolean get() = attachedController != null

  // Lifecycle tracking
  var onAttachCallCount = 0
    private set
  var onDetachCallCount = 0
    private set
  var onCameraReadyCallCount = 0
    private set
  var onCameraReleasedCallCount = 0
    private set

  /**
   * Order of lifecycle calls for verification.
   */
  private val _lifecycleCalls = mutableListOf<LifecycleEvent>()
  val lifecycleCalls: List<LifecycleEvent> get() = _lifecycleCalls

  override fun onAttach(controller: CameraController) {
    onAttachCallCount++
    attachedController = controller
    _lifecycleCalls.add(LifecycleEvent.ATTACH)
  }

  override fun onDetach() {
    onDetachCallCount++
    attachedController = null
    _lifecycleCalls.add(LifecycleEvent.DETACH)
  }

  override fun onCameraReady() {
    onCameraReadyCallCount++
    _lifecycleCalls.add(LifecycleEvent.CAMERA_READY)
  }

  override fun onCameraReleased() {
    onCameraReleasedCallCount++
    _lifecycleCalls.add(LifecycleEvent.CAMERA_RELEASED)
  }

  /**
   * Reset all recorded state.
   */
  fun reset() {
    attachedController = null
    onAttachCallCount = 0
    onDetachCallCount = 0
    onCameraReadyCallCount = 0
    onCameraReleasedCallCount = 0
    _lifecycleCalls.clear()
  }

  enum class LifecycleEvent {
    ATTACH,
    DETACH,
    CAMERA_READY,
    CAMERA_RELEASED,
  }
}
