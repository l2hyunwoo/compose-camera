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
package io.github.l2hyunwoo.compose.camera.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.camera.core.rememberCameraPermissionManager

/**
 * Sample app entry point composable.
 * Handles permission requests and displays camera/gallery screens.
 */
@Composable
fun SampleApp() {
  val permissionManager = rememberCameraPermissionManager()
  var hasPermission by remember { mutableStateOf(false) }
  var permissionChecked by remember { mutableStateOf(false) }
  var currentScreen by remember { mutableStateOf(Screen.Camera) }

  LaunchedEffect(Unit) {
    val result = permissionManager.requestCameraPermissions()
    hasPermission = result.cameraGranted
    permissionChecked = true
  }

  MaterialTheme(
    colorScheme = darkColorScheme(),
  ) {
    when {
      !permissionChecked -> {
        // Loading state while checking permissions
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text("권한 확인 중...", color = MaterialTheme.colorScheme.onBackground)
        }
      }
      hasPermission -> {
        when (currentScreen) {
          Screen.Camera -> {
            CameraScreen(
              onGalleryClick = { currentScreen = Screen.Gallery },
              onBarcodeScannerClick = { currentScreen = Screen.BarcodeScanner },
            )
          }
          Screen.Gallery -> {
            BackHandler(enabled = true) {
              currentScreen = Screen.Camera
            }
            GalleryScreen(
              onBack = { currentScreen = Screen.Camera },
              onItemClick = { /* TODO: Show media detail */ },
            )
          }
          Screen.BarcodeScanner -> {
            BackHandler(enabled = true) {
              currentScreen = Screen.Camera
            }
            BarcodeScannerScreen(
              onBack = { currentScreen = Screen.Camera },
            )
          }
        }
      }
      else -> {
        // Permission denied - show settings button
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Button(onClick = { permissionManager.openAppSettings() }) {
            Text("카메라 권한 설정 열기")
          }
        }
      }
    }
  }
}

private enum class Screen {
  Camera,
  Gallery,
  BarcodeScanner,
}
