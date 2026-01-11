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
package io.github.l2hyunwoo.compose.camera.sample.extensions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraLens
import io.github.l2hyunwoo.compose.camera.core.initialize
import io.github.l2hyunwoo.compose.camera.core.surfaceRequestFlow

/**
 * Sample activity demonstrating custom extensions usage.
 */
class MainActivity : ComponentActivity() {

  private val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      showCamera()
    } else {
      Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    checkPermissionAndShowCamera()
  }

  private fun checkPermissionAndShowCamera() {
    when {
      ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA,
      ) == PackageManager.PERMISSION_GRANTED -> {
        showCamera()
      }

      else -> {
        permissionLauncher.launch(Manifest.permission.CAMERA)
      }
    }
  }

  private fun showCamera() {
    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          CustomExtensionsScreen()
        }
      }
    }
  }
}

@Composable
fun CustomExtensionsScreen() {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Create extensions
  val exposureLockExtension = remember { ExposureLockExtension() }
  val tapCounterExtension = remember { TapCounterExtension() }

  // Create controller and register extensions
  val controller = remember {
    AndroidCameraController(
      context = context,
      lifecycleOwner = lifecycleOwner,
      initialConfiguration = CameraConfiguration(lens = CameraLens.BACK),
    ).also { ctrl ->
      ctrl.registerExtension(exposureLockExtension)
      ctrl.registerExtension(tapCounterExtension)
    }
  }

  // Observe extension states
  val isExposureLocked by exposureLockExtension.isLocked.collectAsState()
  val tapCount by tapCounterExtension.tapCount.collectAsState()

  // Initialize camera
  LaunchedEffect(controller) {
    controller.initialize()
  }

  // Cleanup
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Camera preview
    CustomPreviewWithExtensions(
      controller = controller,
      tapCounterExtension = tapCounterExtension,
      modifier = Modifier.fillMaxSize(),
    )

    // Extension controls overlay
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .background(Color.Black.copy(alpha = 0.5f))
        .statusBarsPadding()
        .padding(16.dp),
    ) {
      Text(
        text = "Tap count: $tapCount",
        color = Color.White,
      )

      Text(
        text = "Exposure: ${if (isExposureLocked) "LOCKED" else "AUTO"}",
        color = if (isExposureLocked) Color.Yellow else Color.White,
      )
    }

    // Control buttons
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      Button(
        onClick = { exposureLockExtension.toggle() },
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isExposureLocked) Color.Yellow else MaterialTheme.colorScheme.primary,
        ),
      ) {
        Text(
          text = if (isExposureLocked) "Unlock AE" else "Lock AE",
          color = if (isExposureLocked) Color.Black else Color.White,
        )
      }

      Button(
        onClick = { tapCounterExtension.reset() },
      ) {
        Text("Reset Counter")
      }
    }
  }
}

@Composable
fun CustomPreviewWithExtensions(
  controller: CameraController,
  tapCounterExtension: TapCounterExtension,
  modifier: Modifier = Modifier,
) {
  val surfaceRequest by controller.surfaceRequestFlow.collectAsState()

  surfaceRequest?.let { request ->
    androidx.camera.compose.CameraXViewfinder(
      surfaceRequest = request,
      modifier = modifier,
    )
  }
}
