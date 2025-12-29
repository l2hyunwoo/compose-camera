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

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PermissionResultTest {
  @Test
  fun allGrantedWhenAllPermissionsGranted() {
    val result = PermissionResult(
      mapOf(
        CameraPermission.CAMERA to PermissionStatus.GRANTED,
        CameraPermission.MICROPHONE to PermissionStatus.GRANTED,
      ),
    )

    result.allGranted shouldBe true
    result.cameraGranted shouldBe true
    result.microphoneGranted shouldBe true
  }

  @Test
  fun allGrantedFalseWhenAnyDenied() {
    val result = PermissionResult(
      mapOf(
        CameraPermission.CAMERA to PermissionStatus.GRANTED,
        CameraPermission.MICROPHONE to PermissionStatus.DENIED,
      ),
    )

    result.allGranted shouldBe false
    result.cameraGranted shouldBe true
    result.microphoneGranted shouldBe false
  }

  @Test
  fun allGrantedFalseWhenAllDenied() {
    val result = PermissionResult(
      mapOf(
        CameraPermission.CAMERA to PermissionStatus.DENIED,
        CameraPermission.MICROPHONE to PermissionStatus.DENIED,
      ),
    )

    result.allGranted shouldBe false
    result.cameraGranted shouldBe false
    result.microphoneGranted shouldBe false
  }

  @Test
  fun cameraGrantedWhenOnlyCameraInMap() {
    val result = PermissionResult(
      mapOf(CameraPermission.CAMERA to PermissionStatus.GRANTED),
    )

    result.cameraGranted shouldBe true
    result.microphoneGranted shouldBe false
  }

  @Test
  fun emptyPermissionsAllGranted() {
    val result = PermissionResult(emptyMap())

    result.allGranted shouldBe true
    result.cameraGranted shouldBe false
    result.microphoneGranted shouldBe false
  }
}
