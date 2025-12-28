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
package io.github.l2hyunwoo.camera.core

/**
 * Camera-related permissions that may need to be requested.
 */
enum class CameraPermission {
  /** Camera access for preview and capture */
  CAMERA,

  /** Microphone access for video recording with audio */
  MICROPHONE,

  /** Storage access for saving photos/videos (Android legacy, API < 29) */
  STORAGE,
}

/**
 * Status of a permission request.
 */
enum class PermissionStatus {
  /** Permission has been granted */
  GRANTED,

  /** Permission has been denied */
  DENIED,

  /** Permission has not been requested yet (iOS only) */
  NOT_DETERMINED,
}

/**
 * Result of requesting camera permissions.
 */
data class PermissionResult(
  val permissions: Map<CameraPermission, PermissionStatus>,
) {
  /** Returns true if all requested permissions were granted */
  val allGranted: Boolean
    get() = permissions.values.all { it == PermissionStatus.GRANTED }

  /** Returns true if camera permission is granted */
  val cameraGranted: Boolean
    get() = permissions[CameraPermission.CAMERA] == PermissionStatus.GRANTED

  /** Returns true if microphone permission is granted */
  val microphoneGranted: Boolean
    get() = permissions[CameraPermission.MICROPHONE] == PermissionStatus.GRANTED
}
