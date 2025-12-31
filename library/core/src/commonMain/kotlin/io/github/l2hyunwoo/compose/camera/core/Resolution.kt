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

import kotlinx.serialization.Serializable

/**
 * Data class representing a camera resolution.
 */
@Serializable
data class Resolution(
  val width: Int,
  val height: Int,
) {
  /**
   * Approximate megapixels for this resolution.
   */
  val megapixels: Float get() = (width * height) / MEGAPIXELS_FACTOR

  override fun toString(): String = "${width}x$height"

  companion object {
    private const val MEGAPIXELS_FACTOR = 1_000_000f
  }
}
