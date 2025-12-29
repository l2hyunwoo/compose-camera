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
package io.github.l2hyunwoo.camera.plugin.mlkit.text

import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import kotlinx.coroutines.flow.StateFlow

/**
 * Result of text recognition containing full text and structured blocks.
 *
 * @property text The full recognized text as a single string
 * @property blocks List of text blocks detected in the image
 */
data class TextResult(
  val text: String,
  val blocks: List<TextBlock>,
)

/**
 * A block of text, typically representing a paragraph or distinct text region.
 *
 * @property text The text content of this block
 * @property lines List of text lines within this block
 */
data class TextBlock(
  val text: String,
  val lines: List<TextLine>,
)

/**
 * A line of text within a text block.
 *
 * @property text The text content of this line
 * @property elements List of text elements (words) within this line
 */
data class TextLine(
  val text: String,
  val elements: List<TextElement>,
)

/**
 * A text element, typically representing a single word.
 *
 * @property text The text content of this element
 */
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
