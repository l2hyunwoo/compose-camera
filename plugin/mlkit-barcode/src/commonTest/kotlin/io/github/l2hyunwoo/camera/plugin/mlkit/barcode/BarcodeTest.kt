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
package io.github.l2hyunwoo.camera.plugin.mlkit.barcode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class BarcodeTest {
  @Test
  fun barcodeCreation() {
    val barcode = Barcode(
      rawValue = "https://example.com",
      format = BarcodeFormat.QR_CODE,
      displayValue = "Example URL",
    )

    assertEquals("https://example.com", barcode.rawValue)
    assertEquals(BarcodeFormat.QR_CODE, barcode.format)
    assertEquals("Example URL", barcode.displayValue)
  }

  @Test
  fun barcodeCreationWithoutDisplayValue() {
    val barcode = Barcode(
      rawValue = "1234567890123",
      format = BarcodeFormat.EAN_13,
    )

    assertEquals("1234567890123", barcode.rawValue)
    assertEquals(BarcodeFormat.EAN_13, barcode.format)
    assertNull(barcode.displayValue)
  }

  @Test
  fun barcodeEquality() {
    val barcode1 = Barcode("123", BarcodeFormat.EAN_13)
    val barcode2 = Barcode("123", BarcodeFormat.EAN_13)

    assertEquals(barcode1, barcode2)
    assertEquals(barcode1.hashCode(), barcode2.hashCode())
  }

  @Test
  fun barcodeInequality() {
    val barcode1 = Barcode("123", BarcodeFormat.EAN_13)
    val barcode2 = Barcode("456", BarcodeFormat.EAN_13)
    val barcode3 = Barcode("123", BarcodeFormat.QR_CODE)

    assertNotEquals(barcode1, barcode2)
    assertNotEquals(barcode1, barcode3)
  }

  @Test
  fun barcodeCopy() {
    val original = Barcode("test", BarcodeFormat.QR_CODE)
    val copied = original.copy(rawValue = "modified")

    assertEquals("test", original.rawValue)
    assertEquals("modified", copied.rawValue)
    assertEquals(original.format, copied.format)
  }
}
