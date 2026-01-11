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

/**
 * Default Android implementation of [ImageCaptureUseCase].
 * Delegates to the controller's internal capture logic.
 */
internal class DefaultAndroidImageCaptureUseCase(
  private val controller: AndroidCameraController,
) : ImageCaptureUseCase {

  override suspend fun capture(
    controller: CameraController,
    config: CaptureConfig,
  ): ImageCaptureResult {
    // For now, delegate to the existing takePicture implementation
    // The config can be used to customize capture in the future
    return this.controller.takePictureInternal()
  }
}
