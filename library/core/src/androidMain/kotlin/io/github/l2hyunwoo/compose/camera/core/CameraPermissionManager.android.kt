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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android implementation of [CameraPermissionManager].
 */
actual class CameraPermissionManager internal constructor(
  private val context: Context,
  private val onRequestPermissions: (Array<String>, (Map<String, Boolean>) -> Unit) -> Unit,
) {
  actual suspend fun checkPermission(permission: CameraPermission): PermissionStatus {
    val androidPermission = permission.toAndroidPermission() ?: return PermissionStatus.GRANTED

    return when (ContextCompat.checkSelfPermission(context, androidPermission)) {
      PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
      else -> PermissionStatus.DENIED
    }
  }

  actual suspend fun requestPermission(permission: CameraPermission): PermissionStatus {
    val androidPermission = permission.toAndroidPermission() ?: return PermissionStatus.GRANTED

    // First check if already granted
    if (ContextCompat.checkSelfPermission(context, androidPermission) == PackageManager.PERMISSION_GRANTED) {
      return PermissionStatus.GRANTED
    }

    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
      onRequestPermissions(arrayOf(androidPermission)) { results ->
        val granted = results[androidPermission] == true
        continuation.resume(
          if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED,
        ) {}
      }
    }
  }

  actual suspend fun requestCameraPermissions(): PermissionResult {
    val permissionsToRequest = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

    // Check which permissions are already granted
    val alreadyGranted = permissionsToRequest.filter {
      ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val needToRequest = permissionsToRequest - alreadyGranted.toSet()

    val resultMap = mutableMapOf<CameraPermission, PermissionStatus>()

    // Add already granted permissions
    if (Manifest.permission.CAMERA in alreadyGranted) {
      resultMap[CameraPermission.CAMERA] = PermissionStatus.GRANTED
    }
    if (Manifest.permission.RECORD_AUDIO in alreadyGranted) {
      resultMap[CameraPermission.MICROPHONE] = PermissionStatus.GRANTED
    }

    // Request remaining permissions
    if (needToRequest.isNotEmpty()) {
      return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        onRequestPermissions(needToRequest.toTypedArray()) { results ->
          if (Manifest.permission.CAMERA in needToRequest) {
            resultMap[CameraPermission.CAMERA] =
              if (results[Manifest.permission.CAMERA] == true) {
                PermissionStatus.GRANTED
              } else {
                PermissionStatus.DENIED
              }
          }
          if (Manifest.permission.RECORD_AUDIO in needToRequest) {
            resultMap[CameraPermission.MICROPHONE] =
              if (results[Manifest.permission.RECORD_AUDIO] == true) {
                PermissionStatus.GRANTED
              } else {
                PermissionStatus.DENIED
              }
          }
          continuation.resume(PermissionResult(resultMap)) {}
        }
      }
    }

    return PermissionResult(resultMap)
  }

  actual fun openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }

  private fun CameraPermission.toAndroidPermission(): String? = when (this) {
    CameraPermission.CAMERA -> Manifest.permission.CAMERA

    CameraPermission.MICROPHONE -> Manifest.permission.RECORD_AUDIO

    CameraPermission.STORAGE -> {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Manifest.permission.WRITE_EXTERNAL_STORAGE
      } else {
        null
      }
    }
  }
}

/**
 * Create Android [CameraPermissionManager] using Compose's ActivityResultLauncher.
 */
@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
  val context = LocalContext.current
  var pendingCallback by remember { mutableStateOf<((Map<String, Boolean>) -> Unit)?>(null) }

  val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
  ) { results ->
    pendingCallback?.invoke(results)
    pendingCallback = null
  }

  return remember(context, launcher) {
    CameraPermissionManager(
      context = context,
      onRequestPermissions = { permissions, callback ->
        pendingCallback = callback
        launcher.launch(permissions)
      },
    )
  }
}
