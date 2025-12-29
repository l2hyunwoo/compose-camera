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

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class CameraExceptionTest {
  @Test
  fun permissionDeniedException() {
    val exception = CameraException.PermissionDenied()

    exception.message shouldBe "Camera permission denied"
    exception.cause shouldBe null
  }

  @Test
  fun noCameraAvailableException() {
    val exception = CameraException.NoCameraAvailable()

    exception.message shouldBe "No camera device available"
  }

  @Test
  fun initializationFailedWithCause() {
    val cause = RuntimeException("Init error")
    val exception = CameraException.InitializationFailed(cause)

    exception.message shouldBe "Camera initialization failed"
    exception.cause shouldBe cause
  }

  @Test
  fun captureFailedWithCause() {
    val cause = IllegalStateException("Capture error")
    val exception = CameraException.CaptureFailed(cause)

    exception.message shouldBe "Image capture failed"
    exception.cause shouldBe cause
  }

  @Test
  fun recordingFailedWithCause() {
    val cause = IllegalStateException("Recording error")
    val exception = CameraException.RecordingFailed(cause)

    exception.message shouldBe "Video recording failed"
    exception.cause shouldBe cause
  }

  @Test
  fun unknownExceptionWithMessage() {
    val exception = CameraException.Unknown("Custom error message")

    exception.message shouldBe "Custom error message"
    exception.cause shouldBe null
  }

  @Test
  fun allExceptionsAreCameraExceptions() {
    val exceptions = listOf(
      CameraException.PermissionDenied(),
      CameraException.NoCameraAvailable(),
      CameraException.InitializationFailed(),
      CameraException.CaptureFailed(),
      CameraException.RecordingFailed(),
      CameraException.Unknown("test"),
    )

    exceptions.forEach { exception ->
      exception.shouldBeInstanceOf<CameraException>()
      exception.shouldBeInstanceOf<Exception>()
    }
  }
}
