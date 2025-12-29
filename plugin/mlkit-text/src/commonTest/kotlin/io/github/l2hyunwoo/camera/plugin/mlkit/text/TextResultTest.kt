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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextResultTest {
  @Test
  fun textResultCreation() {
    val element = TextElement("Hello")
    val line = TextLine("Hello World", listOf(element))
    val block = TextBlock("Hello World Block", listOf(line))
    val result = TextResult("Hello World", listOf(block))

    assertEquals("Hello World", result.text)
    assertEquals(1, result.blocks.size)
    assertEquals("Hello World Block", result.blocks.first().text)
  }

  @Test
  fun emptyTextResult() {
    val result = TextResult("", emptyList())

    assertEquals("", result.text)
    assertTrue(result.blocks.isEmpty())
  }

  @Test
  fun textResultEquality() {
    val result1 = TextResult("Test", emptyList())
    val result2 = TextResult("Test", emptyList())

    assertEquals(result1, result2)
    assertEquals(result1.hashCode(), result2.hashCode())
  }
}

class TextBlockTest {
  @Test
  fun textBlockCreation() {
    val line = TextLine("Line 1", emptyList())
    val block = TextBlock("Block Text", listOf(line))

    assertEquals("Block Text", block.text)
    assertEquals(1, block.lines.size)
  }

  @Test
  fun textBlockWithMultipleLines() {
    val lines = listOf(
      TextLine("Line 1", emptyList()),
      TextLine("Line 2", emptyList()),
      TextLine("Line 3", emptyList()),
    )
    val block = TextBlock("Full block", lines)

    assertEquals(3, block.lines.size)
    assertEquals("Line 1", block.lines[0].text)
    assertEquals("Line 3", block.lines[2].text)
  }
}

class TextLineTest {
  @Test
  fun textLineCreation() {
    val elements = listOf(TextElement("Hello"), TextElement("World"))
    val line = TextLine("Hello World", elements)

    assertEquals("Hello World", line.text)
    assertEquals(2, line.elements.size)
  }

  @Test
  fun textLineWithEmptyElements() {
    val line = TextLine("Empty line", emptyList())

    assertEquals("Empty line", line.text)
    assertTrue(line.elements.isEmpty())
  }
}

class TextElementTest {
  @Test
  fun textElementCreation() {
    val element = TextElement("Word")

    assertEquals("Word", element.text)
  }

  @Test
  fun textElementEquality() {
    val element1 = TextElement("Same")
    val element2 = TextElement("Same")

    assertEquals(element1, element2)
  }

  @Test
  fun textElementCopy() {
    val original = TextElement("Original")
    val copied = original.copy(text = "Copied")

    assertEquals("Original", original.text)
    assertEquals("Copied", copied.text)
  }
}
