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
 * Platform-agnostic representation of a focus point.
 * Coordinates are normalized to 0-1 range where (0,0) is top-left and (1,1) is bottom-right.
 *
 * @property x Horizontal position (0.0 = left, 1.0 = right)
 * @property y Vertical position (0.0 = top, 1.0 = bottom)
 */
data class FocusPoint(
  val x: Float,
  val y: Float,
) {
  init {
    require(x in 0f..1f) { "x must be in range [0, 1], was $x" }
    require(y in 0f..1f) { "y must be in range [0, 1], was $y" }
  }

  companion object {
    /**
     * Center of the preview area
     */
    val CENTER = FocusPoint(0.5f, 0.5f)

    /**
     * Create a FocusPoint with values clamped to valid range
     */
    fun clamped(x: Float, y: Float): FocusPoint = FocusPoint(
      x = x.coerceIn(0f, 1f),
      y = y.coerceIn(0f, 1f),
    )
  }
}
