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

import io.github.l2hyunwoo.compose.camera.core.plugin.CameraFrame
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CameraFramePropertyTest {
  @Test
  fun cameraFrameEqualityWithSameData() = runTest {
    checkAll(Arb.string(), Arb.int(1, 1920), Arb.int(1, 1080)) { format, width, height ->
      val data = byteArrayOf(1, 2, 3)
      val frame1 = CameraFrame(
        data = data,
        width = width,
        height = height,
        format = format,
        rotation = 0,
        timestamp = 0L,
      )
      val frame2 = CameraFrame(
        data = data,
        width = width,
        height = height,
        format = format,
        rotation = 0,
        timestamp = 0L,
      )

      frame1 shouldBe frame2
      frame1.hashCode() shouldBe frame2.hashCode()
    }
  }

  @Test
  fun cameraFrameInequalityWithDifferentData() {
    val data1 = byteArrayOf(1, 2, 3)
    val data2 = byteArrayOf(4, 5, 6)

    val frame1 = CameraFrame(
      data = data1,
      width = 100,
      height = 100,
      format = "YUV_420_888",
      rotation = 0,
      timestamp = 0L,
    )
    val frame2 = CameraFrame(
      data = data2,
      width = 100,
      height = 100,
      format = "YUV_420_888",
      rotation = 0,
      timestamp = 0L,
    )

    frame1 shouldNotBe frame2
  }
}
