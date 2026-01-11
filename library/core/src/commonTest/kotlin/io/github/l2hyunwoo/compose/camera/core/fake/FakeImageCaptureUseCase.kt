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
import io.github.l2hyunwoo.compose.camera.core.CaptureConfig
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureUseCase

/**
 * Fake implementation of [ImageCaptureUseCase] for testing.
 */
class FakeImageCaptureUseCase : ImageCaptureUseCase {

  /**
   * Configure the result to return from capture().
   */
  var captureResult: ImageCaptureResult = ImageCaptureResult.Success(
    byteArray = ByteArray(0),
    width = 1920,
    height = 1080,
  )

  /**
   * If set, capture() will throw this exception.
   */
  var captureException: Throwable? = null

  /**
   * Records all capture calls with their configs.
   */
  private val _captureCalls = mutableListOf<CaptureCall>()
  val captureCalls: List<CaptureCall> get() = _captureCalls

  /**
   * Number of times capture() was called.
   */
  val captureCallCount: Int get() = _captureCalls.size

  override suspend fun capture(controller: CameraController, config: CaptureConfig): ImageCaptureResult {
    captureException?.let { throw it }
    _captureCalls.add(CaptureCall(controller, config))
    return captureResult
  }

  /**
   * Reset recorded state.
   */
  fun reset() {
    _captureCalls.clear()
    captureException = null
    captureResult = ImageCaptureResult.Success(
      byteArray = ByteArray(0),
      width = 1920,
      height = 1080,
    )
  }

  data class CaptureCall(
    val controller: CameraController,
    val config: CaptureConfig,
  )
}
