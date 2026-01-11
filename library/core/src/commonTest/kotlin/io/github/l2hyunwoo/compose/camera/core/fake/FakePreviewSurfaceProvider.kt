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

import io.github.l2hyunwoo.compose.camera.core.PreviewSurfaceProvider

/**
 * Fake implementation of [PreviewSurfaceProvider] for testing.
 * Records all calls for verification.
 */
class FakePreviewSurfaceProvider : PreviewSurfaceProvider {

  /**
   * The last surface provided via onSurfaceAvailable.
   */
  var lastSurface: Any? = null
    private set

  /**
   * Whether onSurfaceDestroyed was called.
   */
  var isDestroyed: Boolean = false
    private set

  // Call tracking
  var onSurfaceAvailableCallCount = 0
    private set
  var onSurfaceDestroyedCallCount = 0
    private set

  private val _surfaceAvailableCalls = mutableListOf<Any>()
  val surfaceAvailableCalls: List<Any> get() = _surfaceAvailableCalls

  override fun onSurfaceAvailable(surface: Any) {
    onSurfaceAvailableCallCount++
    lastSurface = surface
    isDestroyed = false
    _surfaceAvailableCalls.add(surface)
  }

  override fun onSurfaceDestroyed() {
    onSurfaceDestroyedCallCount++
    isDestroyed = true
    lastSurface = null
  }

  /**
   * Reset all recorded state.
   */
  fun reset() {
    lastSurface = null
    isDestroyed = false
    onSurfaceAvailableCallCount = 0
    onSurfaceDestroyedCallCount = 0
    _surfaceAvailableCalls.clear()
  }
}
