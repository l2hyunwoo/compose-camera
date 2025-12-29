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
import kotlin.test.assertTrue

class BarcodeFormatTest {
  @Test
  fun allFormatsExist() {
    val formats = BarcodeFormat.entries

    assertTrue(formats.contains(BarcodeFormat.QR_CODE))
    assertTrue(formats.contains(BarcodeFormat.AZTEC))
    assertTrue(formats.contains(BarcodeFormat.DATA_MATRIX))
    assertTrue(formats.contains(BarcodeFormat.PDF417))
    assertTrue(formats.contains(BarcodeFormat.EAN_13))
    assertTrue(formats.contains(BarcodeFormat.EAN_8))
    assertTrue(formats.contains(BarcodeFormat.UPC_A))
    assertTrue(formats.contains(BarcodeFormat.UPC_E))
    assertTrue(formats.contains(BarcodeFormat.CODE_39))
    assertTrue(formats.contains(BarcodeFormat.CODE_93))
    assertTrue(formats.contains(BarcodeFormat.CODE_128))
    assertTrue(formats.contains(BarcodeFormat.CODABAR))
    assertTrue(formats.contains(BarcodeFormat.ITF))
    assertTrue(formats.contains(BarcodeFormat.UNKNOWN))
  }

  @Test
  fun formatCount() {
    assertEquals(14, BarcodeFormat.entries.size)
  }

  @Test
  fun formatOrdinals() {
    assertEquals(0, BarcodeFormat.QR_CODE.ordinal)
    assertEquals(13, BarcodeFormat.UNKNOWN.ordinal)
  }

  @Test
  fun formatNames() {
    assertEquals("QR_CODE", BarcodeFormat.QR_CODE.name)
    assertEquals("EAN_13", BarcodeFormat.EAN_13.name)
    assertEquals("UNKNOWN", BarcodeFormat.UNKNOWN.name)
  }
}
