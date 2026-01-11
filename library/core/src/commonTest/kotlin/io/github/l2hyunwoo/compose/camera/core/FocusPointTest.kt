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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FocusPointTest :
  FunSpec({

    context("FocusPoint CENTER") {
      test("FocusPoint CENTER equals (0.5, 0.5)") {
        val center = FocusPoint(0.5f, 0.5f)
        center shouldBe FocusPoint.CENTER
      }
    }

    context("FocusPoint valid coordinates") {
      test("FocusPoint accepts valid coordinates in 0-1 range") {
        // Corner cases
        val topLeft = FocusPoint(0f, 0f)
        topLeft.x shouldBe 0f
        topLeft.y shouldBe 0f

        val bottomRight = FocusPoint(1f, 1f)
        bottomRight.x shouldBe 1f
        bottomRight.y shouldBe 1f

        // Middle values
        val middle = FocusPoint(0.25f, 0.75f)
        middle.x shouldBe 0.25f
        middle.y shouldBe 0.75f
      }
    }

    context("FocusPoint invalid coordinates") {
      test("FocusPoint rejects x value below 0") {
        shouldThrow<IllegalArgumentException> {
          FocusPoint(-0.1f, 0.5f)
        }
      }

      test("FocusPoint rejects x value above 1") {
        shouldThrow<IllegalArgumentException> {
          FocusPoint(1.1f, 0.5f)
        }
      }

      test("FocusPoint rejects y value below 0") {
        shouldThrow<IllegalArgumentException> {
          FocusPoint(0.5f, -0.1f)
        }
      }

      test("FocusPoint rejects y value above 1") {
        shouldThrow<IllegalArgumentException> {
          FocusPoint(0.5f, 1.1f)
        }
      }
    }

    context("FocusPoint.clamped") {
      test("clamped clamps values below 0") {
        val clamped = FocusPoint.clamped(-0.5f, -0.3f)
        clamped.x shouldBe 0f
        clamped.y shouldBe 0f
      }

      test("clamped clamps values above 1") {
        val clamped = FocusPoint.clamped(1.5f, 2.0f)
        clamped.x shouldBe 1f
        clamped.y shouldBe 1f
      }

      test("clamped preserves valid values") {
        val clamped = FocusPoint.clamped(0.3f, 0.7f)
        clamped.x shouldBe 0.3f
        clamped.y shouldBe 0.7f
      }

      test("clamped handles mixed valid and invalid values") {
        val clamped = FocusPoint.clamped(-0.2f, 0.8f)
        clamped.x shouldBe 0f
        clamped.y shouldBe 0.8f
      }
    }

    context("FocusPoint data class functionality") {
      test("equality works correctly") {
        val point1 = FocusPoint(0.3f, 0.7f)
        val point2 = FocusPoint(0.3f, 0.7f)
        val point3 = FocusPoint(0.4f, 0.7f)

        point1 shouldBe point2
        (point1 == point3) shouldBe false
      }

      test("copy works correctly") {
        val original = FocusPoint(0.3f, 0.7f)
        val copied = original.copy(x = 0.5f)

        copied.x shouldBe 0.5f
        copied.y shouldBe 0.7f
      }
    }
  })
