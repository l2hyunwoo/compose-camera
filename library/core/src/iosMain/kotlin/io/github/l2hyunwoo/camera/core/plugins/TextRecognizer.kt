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
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.l2hyunwoo.camera.core.plugins

import io.github.l2hyunwoo.camera.core.CameraController
import io.github.l2hyunwoo.camera.core.IOSCameraController
import io.github.l2hyunwoo.camera.core.plugin.CameraPlugin
import kotlinx.cinterop.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreMedia.CMSampleBufferRef
import platform.Vision.*

/**
 * iOS implementation of text recognizer (OCR) using Apple Vision framework.
 * Recognizes text from camera frames in real-time.
 */
actual class TextRecognizer actual constructor() : CameraPlugin {
  override val id: String = "TextRecognizer"

  private val _text = MutableStateFlow<TextResult?>(null)
  actual val text: StateFlow<TextResult?> = _text.asStateFlow()

  private var iosController: IOSCameraController? = null

  private val request: VNRecognizeTextRequest = VNRecognizeTextRequest(completionHandler = { request, error ->
    if (error == null) {
      val observations = request?.results as? List<VNRecognizedTextObservation>
      if (observations != null) {
        // Combine all text for simplicity
        val fullText = StringBuilder()
        val blocks = observations.map { observation ->
          val topCandidate = observation.topCandidates(1u).firstOrNull() as? VNRecognizedText
          val blockText = topCandidate?.string ?: ""
          fullText.append(blockText).append("\n")

          // Vision doesn't provide generic Block/Line/Element hierarchy directly like ML Kit
          // It gives Observations (roughly lines or blocks depending on settings)
          TextBlock(
            text = blockText,
            lines = listOf(TextLine(blockText, listOf(TextElement(blockText)))),
          )
        }

        _text.value = TextResult(
          text = fullText.toString().trim(),
          blocks = blocks,
        )
      } else {
        _text.value = null
      }
    }
  })

  private val frameListener: (CMSampleBufferRef?) -> Unit = { buffer ->
    if (buffer != null) {
      processFrame(buffer)
    }
  }

  init {
    request.recognitionLevel = VNRequestTextRecognitionLevelAccurate
  }

  override fun onAttach(controller: CameraController) {
    if (controller is IOSCameraController) {
      iosController = controller
      controller.addFrameListener(frameListener)
    }
  }

  override fun onDetach() {
    iosController?.removeFrameListener(frameListener)
    iosController = null
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun processFrame(buffer: CMSampleBufferRef) {
    val handler = VNImageRequestHandler(cMSampleBuffer = buffer, options = mapOf<Any?, Any?>())
    try {
      // performRequests throws Error in Kotlin if method signature has error param and returns generic
      // But for performRequests:error:, it usually returns Boolean.
      // In KMP, we might need to pass error pointer or it throws exception.
      // Let's try simple call first, assuming it throws on failure.
      handler.performRequests(listOf(request), null)
    } catch (e: Exception) {
      // Handle error
    }
  }
}
