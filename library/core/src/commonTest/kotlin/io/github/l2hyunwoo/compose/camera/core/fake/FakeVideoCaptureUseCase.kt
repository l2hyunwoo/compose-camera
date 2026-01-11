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
package io.github.l2hyunwoo.compose.camera.core.fake

import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.RecordingConfig
import io.github.l2hyunwoo.compose.camera.core.VideoCaptureUseCase
import io.github.l2hyunwoo.compose.camera.core.VideoRecording
import io.github.l2hyunwoo.compose.camera.core.VideoRecordingResult

/**
 * Fake implementation of [VideoCaptureUseCase] for testing.
 */
class FakeVideoCaptureUseCase : VideoCaptureUseCase {

  /**
   * If set, startRecording() will throw this exception.
   */
  var startException: Throwable? = null

  /**
   * Records all startRecording calls.
   */
  private val _startRecordingCalls = mutableListOf<RecordingCall>()
  val startRecordingCalls: List<RecordingCall> get() = _startRecordingCalls

  /**
   * The result to return when stop() is called on the recording.
   */
  var stopResult: VideoRecordingResult = VideoRecordingResult.Success(
    uri = "file:///test/video.mp4",
    durationMs = 5000L,
  )

  override suspend fun startRecording(controller: CameraController, config: RecordingConfig): VideoRecording {
    startException?.let { throw it }
    _startRecordingCalls.add(RecordingCall(controller, config))
    return FakeVideoRecording(stopResult)
  }

  /**
   * Reset recorded state.
   */
  fun reset() {
    _startRecordingCalls.clear()
    startException = null
    stopResult = VideoRecordingResult.Success(
      uri = "file:///test/video.mp4",
      durationMs = 5000L,
    )
  }

  data class RecordingCall(
    val controller: CameraController,
    val config: RecordingConfig,
  )
}

/**
 * Fake implementation of [VideoRecording] for testing.
 */
class FakeVideoRecording(
  private val stopResult: VideoRecordingResult,
) : VideoRecording {

  private var _isRecording = true
  override val isRecording: Boolean get() = _isRecording

  private var _isPaused = false
  val isPaused: Boolean get() = _isPaused

  var stopCallCount = 0
    private set
  var pauseCallCount = 0
    private set
  var resumeCallCount = 0
    private set

  override suspend fun stop(): VideoRecordingResult {
    stopCallCount++
    _isRecording = false
    return stopResult
  }

  override fun pause() {
    pauseCallCount++
    _isPaused = true
  }

  override fun resume() {
    resumeCallCount++
    _isPaused = false
  }
}
