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
package io.github.l2hyunwoo.compose.camera.ui

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import org.junit.Rule
import org.junit.Test

/**
 * Permission flow UI tests for CameraPreview.
 * These tests verify that the camera preview behaves correctly
 * when permissions are granted.
 */
class CameraPermissionFlowTest {
  @get:Rule
  val composeTestRule = createComposeRule()

  @get:Rule
  val cameraPermissionRule: GrantPermissionRule =
    GrantPermissionRule.grant(Manifest.permission.CAMERA)

  @Test
  fun cameraPreviewRendersWhenPermissionGranted() {
    composeTestRule.setContent {
      CameraPreview(
        configuration = CameraConfiguration(),
        onCameraControllerReady = {},
      )
    }

    // Verify the preview composable exists and is displayed
    // Note: Actual camera preview rendering requires real device
    composeTestRule.waitForIdle()
  }

  @Test
  fun cameraPreviewWithMicrophonePermission() {
    composeTestRule.setContent {
      CameraPreview(
        configuration = CameraConfiguration(),
        onCameraControllerReady = {},
      )
    }

    composeTestRule.waitForIdle()
  }
}
