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
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class ImageCaptureResultTest {
  @Test
  fun successResultCreation() {
    val data = byteArrayOf(1, 2, 3, 4, 5)
    val result = ImageCaptureResult.Success(
      byteArray = data,
      width = 1920,
      height = 1080,
      rotation = 90,
      filePath = "/path/to/image.jpg",
    )

    result.byteArray shouldBe data
    result.width shouldBe 1920
    result.height shouldBe 1080
    result.rotation shouldBe 90
    result.filePath shouldBe "/path/to/image.jpg"
  }

  @Test
  fun successResultDefaultValues() {
    val data = byteArrayOf(1, 2, 3)
    val result = ImageCaptureResult.Success(
      byteArray = data,
      width = 640,
      height = 480,
    )

    result.rotation shouldBe 0
    result.filePath shouldBe null
  }

  @Test
  fun successResultEqualityWithSameByteArray() {
    val data = byteArrayOf(1, 2, 3)
    val result1 = ImageCaptureResult.Success(data, 100, 100)
    val result2 = ImageCaptureResult.Success(data, 100, 100)

    result1 shouldBe result2
    result1.hashCode() shouldBe result2.hashCode()
  }

  @Test
  fun successResultInequalityWithDifferentByteArray() {
    val result1 = ImageCaptureResult.Success(byteArrayOf(1, 2, 3), 100, 100)
    val result2 = ImageCaptureResult.Success(byteArrayOf(4, 5, 6), 100, 100)

    result1 shouldNotBe result2
  }

  @Test
  fun errorResultCreation() {
    val exception = CameraException.CaptureFailed()
    val result = ImageCaptureResult.Error(exception)

    result.exception shouldBe exception
  }
}
