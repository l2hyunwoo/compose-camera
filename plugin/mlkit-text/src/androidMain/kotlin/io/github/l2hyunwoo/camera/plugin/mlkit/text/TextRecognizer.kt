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

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.l2hyunwoo.camera.core.AndroidCameraController
import io.github.l2hyunwoo.camera.core.CameraController
import io.github.l2hyunwoo.camera.core.plugin.CameraPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of text recognizer (OCR) using Google ML Kit.
 * Recognizes text from camera frames in real-time.
 */
actual class TextRecognizer actual constructor() : CameraPlugin {
  override val id: String = "TextRecognizer"

  private val _text = MutableStateFlow<TextResult?>(null)
  actual val text: StateFlow<TextResult?> = _text.asStateFlow()

  private val recognizer by lazy {
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
  }

  private var androidController: AndroidCameraController? = null

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
    recognizer.close()
  }

  @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
  private fun processImage(imageProxy: ImageProxy) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
      val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
      recognizer.process(image)
        .addOnSuccessListener { visionText ->
          _text.value = visionText.toCommon()
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

  private fun Text.toCommon(): TextResult {
    return TextResult(
      text = this.text,
      blocks = this.textBlocks.map { block ->
        TextBlock(
          text = block.text,
          lines = block.lines.map { line ->
            TextLine(
              text = line.text,
              elements = line.elements.map { element ->
                TextElement(text = element.text)
              },
            )
          },
        )
      },
    )
  }
}
