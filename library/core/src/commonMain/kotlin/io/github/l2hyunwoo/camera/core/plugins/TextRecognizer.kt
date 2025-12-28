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
package io.github.l2hyunwoo.compose.camera.plugins

import io.github.l2hyunwoo.compose.camera.plugin.CameraPlugin
import kotlinx.coroutines.flow.StateFlow

data class TextResult(
  val text: String,
  val blocks: List<TextBlock>,
)

data class TextBlock(
  val text: String,
  val lines: List<TextLine>,
)

data class TextLine(
  val text: String,
  val elements: List<TextElement>,
)

data class TextElement(
  val text: String,
)

/**
 * Plugin for Recognizing Text (OCR).
 */
expect class TextRecognizer() : CameraPlugin {
  /**
   * Stream of recognized text.
   */
  val text: StateFlow<TextResult?>
}
