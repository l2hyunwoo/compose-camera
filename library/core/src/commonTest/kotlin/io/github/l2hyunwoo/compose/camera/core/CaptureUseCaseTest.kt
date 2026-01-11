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

import io.github.l2hyunwoo.compose.camera.core.fake.FakeCameraController
import io.github.l2hyunwoo.compose.camera.core.fake.FakeImageCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.fake.FakeVideoCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.fake.FakeVideoRecording
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class CaptureUseCaseTest :
  FunSpec({

    lateinit var controller: FakeCameraController

    beforeTest {
      controller = FakeCameraController()
      controller.initialize()
    }

    context("ImageCaptureUseCase Tests") {
      test("capture returns ImageCaptureResult Success on success") {
        val useCase = FakeImageCaptureUseCase()
        val expectedResult = ImageCaptureResult.Success(
          byteArray = byteArrayOf(1, 2, 3),
          width = 1920,
          height = 1080,
        )
        useCase.captureResult = expectedResult

        controller.imageCaptureUseCase = useCase
        val result = controller.takePicture()

        result shouldBe expectedResult
        useCase.captureCallCount shouldBe 1
      }

      test("capture returns ImageCaptureResult Error on failure") {
        val useCase = FakeImageCaptureUseCase()
        val expectedError = ImageCaptureResult.Error(CameraException.CaptureFailed())
        useCase.captureResult = expectedError

        controller.imageCaptureUseCase = useCase
        val result = controller.takePicture()

        result.shouldBeInstanceOf<ImageCaptureResult.Error>()
      }

      test("capture throws when use case throws") {
        val useCase = FakeImageCaptureUseCase()
        useCase.captureException = RuntimeException("Test error")

        controller.imageCaptureUseCase = useCase

        shouldThrow<RuntimeException> {
          controller.takePicture()
        }
      }

      test("capture passes CaptureConfig to use case") {
        val useCase = FakeImageCaptureUseCase()
        val config = CameraConfiguration(
          flashMode = FlashMode.ON,
          imageFormat = ImageFormat.PNG,
          photoResolution = Resolution(4032, 3024),
        )
        controller.updateConfiguration(config)
        controller.imageCaptureUseCase = useCase

        controller.takePicture()

        useCase.captureCalls.size shouldBe 1
        val captureConfig = useCase.captureCalls[0].config
        captureConfig.flashMode shouldBe FlashMode.ON
        captureConfig.outputFormat shouldBe ImageFormat.PNG
        captureConfig.resolution shouldBe Resolution(4032, 3024)
      }
    }

    context("VideoCaptureUseCase Tests") {
      test("startRecording begins recording") {
        val useCase = FakeVideoCaptureUseCase()
        controller.videoCaptureUseCase = useCase

        val recording = controller.startRecording()

        recording.isRecording shouldBe true
        useCase.startRecordingCalls.size shouldBe 1
      }

      test("stopRecording returns VideoRecordingResult") {
        val expectedResult = VideoRecordingResult.Success(
          uri = "file:///custom/video.mp4",
          durationMs = 10000L,
        )
        val useCase = FakeVideoCaptureUseCase()
        useCase.stopResult = expectedResult
        controller.videoCaptureUseCase = useCase

        val recording = controller.startRecording()
        val result = recording.stop()

        result shouldBe expectedResult
      }

      test("recording can be paused and resumed") {
        val useCase = FakeVideoCaptureUseCase()
        controller.videoCaptureUseCase = useCase

        val recording = controller.startRecording() as FakeVideoRecording

        recording.pause()
        recording.isPaused shouldBe true
        recording.pauseCallCount shouldBe 1

        recording.resume()
        recording.isPaused shouldBe false
        recording.resumeCallCount shouldBe 1
      }

      test("startRecording passes RecordingConfig to use case") {
        val useCase = FakeVideoCaptureUseCase()
        val config = CameraConfiguration(
          videoQuality = VideoQuality.UHD,
        )
        controller.updateConfiguration(config)
        controller.videoCaptureUseCase = useCase

        controller.startRecording()

        useCase.startRecordingCalls.size shouldBe 1
        val recordingConfig = useCase.startRecordingCalls[0].config
        recordingConfig.quality shouldBe VideoQuality.UHD
      }
    }

    context("UseCase Injection Tests") {
      test("custom imageCaptureUseCase replaces default") {
        val customUseCase = FakeImageCaptureUseCase()
        val customResult = ImageCaptureResult.Success(
          byteArray = byteArrayOf(42),
          width = 100,
          height = 100,
        )
        customUseCase.captureResult = customResult

        controller.imageCaptureUseCase = customUseCase
        val result = controller.takePicture()

        result shouldBe customResult
        customUseCase.captureCallCount shouldBe 1
      }

      test("custom videoCaptureUseCase replaces default") {
        val customUseCase = FakeVideoCaptureUseCase()
        val customResult = VideoRecordingResult.Success(
          uri = "file:///custom/path.mp4",
          durationMs = 12345L,
        )
        customUseCase.stopResult = customResult

        controller.videoCaptureUseCase = customUseCase
        val recording = controller.startRecording()
        val result = recording.stop()

        result shouldBe customResult
      }
    }

    context("CaptureConfig and RecordingConfig data classes") {
      test("CaptureConfig default values") {
        val config = CaptureConfig()

        config.flashMode shouldBe FlashMode.OFF
        config.resolution shouldBe null
        config.outputFormat shouldBe ImageFormat.JPEG
        config.extras shouldBe emptyMap()
      }

      test("CaptureConfig custom values") {
        val config = CaptureConfig(
          flashMode = FlashMode.AUTO,
          resolution = Resolution(3840, 2160),
          outputFormat = ImageFormat.PNG,
          extras = mapOf("quality" to 95),
        )

        config.flashMode shouldBe FlashMode.AUTO
        config.resolution shouldBe Resolution(3840, 2160)
        config.outputFormat shouldBe ImageFormat.PNG
        config.extras shouldBe mapOf("quality" to 95)
      }

      test("RecordingConfig default values") {
        val config = RecordingConfig()

        config.quality shouldBe VideoQuality.FHD
        config.enableAudio shouldBe true
        config.maxDurationMs shouldBe null
        config.maxFileSizeBytes shouldBe null
        config.extras shouldBe emptyMap()
      }

      test("RecordingConfig custom values") {
        val config = RecordingConfig(
          quality = VideoQuality.UHD,
          enableAudio = false,
          maxDurationMs = 60000L,
          maxFileSizeBytes = 100_000_000L,
          extras = mapOf("bitrate" to 20_000_000),
        )

        config.quality shouldBe VideoQuality.UHD
        config.enableAudio shouldBe false
        config.maxDurationMs shouldBe 60000L
        config.maxFileSizeBytes shouldBe 100_000_000L
        config.extras shouldBe mapOf("bitrate" to 20_000_000)
      }
    }
  })
