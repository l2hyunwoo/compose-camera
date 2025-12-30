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

import androidx.compose.runtime.Composable

/**
 * Platform-specific permission manager for camera-related permissions.
 *
 * Use [rememberCameraPermissionManager] to create an instance.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class CameraPermissionManager {
  /**
   * Check the current status of a permission without requesting it.
   */
  suspend fun checkPermission(permission: CameraPermission): PermissionStatus

  /**
   * Request a single permission.
   * @return The resulting permission status after the request
   */
  suspend fun requestPermission(permission: CameraPermission): PermissionStatus

  /**
   * Request all camera-related permissions (camera and microphone).
   * @return Map of permission to its status
   */
  suspend fun requestCameraPermissions(): PermissionResult

  /**
   * Open the system app settings page where user can manually grant permissions.
   */
  fun openAppSettings()
}

/**
 * Create a platform-specific [CameraPermissionManager] instance.
 *
 * This is a Composable function that provides proper context handling
 * on each platform automatically.
 */
@Composable
expect fun rememberCameraPermissionManager(): CameraPermissionManager
