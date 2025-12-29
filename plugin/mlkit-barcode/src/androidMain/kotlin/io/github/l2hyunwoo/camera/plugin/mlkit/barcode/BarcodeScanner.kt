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

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.plugin.CameraPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.mlkit.vision.barcode.common.Barcode as MlKitBarcode

/**
 * Android implementation of barcode scanner using Google ML Kit.
 * Detects barcodes and QR codes from camera frames.
 */
actual class BarcodeScanner actual constructor() : CameraPlugin {
  override val id: String = "BarcodeScanner"

  private val _barcodes = MutableStateFlow<List<Barcode>>(emptyList())
  actual val barcodes: StateFlow<List<Barcode>> = _barcodes.asStateFlow()

  private val scanner by lazy {
    val options = BarcodeScannerOptions.Builder()
      .setBarcodeFormats(MlKitBarcode.FORMAT_ALL_FORMATS)
      .build()
    BarcodeScanning.getClient(options)
  }

  private var androidController: AndroidCameraController? = null

  // Analyzer implementation
  private val analyzer = ImageAnalysis.Analyzer { imageProxy ->
    processImage(imageProxy)
  }

  override fun onAttach(controller: CameraController) {
    if (controller is AndroidCameraController) {
      androidController = controller
      controller.addAnalyzer(analyzer)
    }
  }

  override fun onDetach() {
    androidController?.removeAnalyzer(analyzer)
    androidController = null
    scanner.close()
  }

  @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
  private fun processImage(imageProxy: ImageProxy) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
      val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
      scanner.process(image)
        .addOnSuccessListener { mlBarcodes ->
          _barcodes.value = mlBarcodes.map { it.toCommon() }
        }
        .addOnFailureListener {
          // Ignore failures
        }
        .addOnCompleteListener {
          imageProxy.close()
        }
    } else {
      imageProxy.close()
    }
  }

  private fun MlKitBarcode.toCommon(): Barcode {
    return Barcode(
      rawValue = this.rawValue ?: "",
      displayValue = this.displayValue,
      format = when (this.format) {
        MlKitBarcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        MlKitBarcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        MlKitBarcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        MlKitBarcode.FORMAT_PDF417 -> BarcodeFormat.PDF417
        MlKitBarcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        MlKitBarcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        MlKitBarcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        MlKitBarcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        MlKitBarcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        MlKitBarcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        MlKitBarcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        MlKitBarcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        MlKitBarcode.FORMAT_ITF -> BarcodeFormat.ITF
        else -> BarcodeFormat.UNKNOWN
      },
    )
  }
}
