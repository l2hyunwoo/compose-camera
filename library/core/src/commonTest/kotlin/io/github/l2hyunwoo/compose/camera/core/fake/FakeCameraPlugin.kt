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

import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin

/**
 * Fake implementation of [CameraPlugin] for testing.
 */
class FakeCameraPlugin(
  override val id: String = "fake-plugin",
) : CameraPlugin {

  var attachedController: CameraController? = null
    private set

  var onAttachCallCount = 0
    private set

  var onDetachCallCount = 0
    private set

  val isAttached: Boolean get() = attachedController != null

  override fun onAttach(controller: CameraController) {
    onAttachCallCount++
    attachedController = controller
  }

  override fun onDetach() {
    onDetachCallCount++
    attachedController = null
  }

  fun reset() {
    attachedController = null
    onAttachCallCount = 0
    onDetachCallCount = 0
  }
}
