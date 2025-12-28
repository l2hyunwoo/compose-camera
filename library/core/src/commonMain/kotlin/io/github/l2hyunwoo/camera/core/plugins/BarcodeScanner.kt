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

/**
 * Generic Barcode data class
 */
data class Barcode(
  val rawValue: String,
  val format: BarcodeFormat,
  val displayValue: String? = null,
)

/**
 * Supported barcode formats
 */
enum class BarcodeFormat {
  QR_CODE,
  AZTEC,
  DATA_MATRIX,
  PDF417,
  EAN_13,
  EAN_8,
  UPC_A,
  UPC_E,
  CODE_39,
  CODE_93,
  CODE_128,
  CODABAR,
  ITF,
  UNKNOWN,
}

/**
 * Plugin for scanning barcodes and QR codes.
 */
expect class BarcodeScanner() : CameraPlugin {
  /**
   * Stream of detected barcodes.
   * Emits a list of barcodes detected in the current frame.
   */
  val barcodes: StateFlow<List<Barcode>>
}
