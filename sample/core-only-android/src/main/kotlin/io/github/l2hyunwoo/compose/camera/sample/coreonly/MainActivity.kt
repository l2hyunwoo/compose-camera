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
package io.github.l2hyunwoo.compose.camera.sample.coreonly

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraController
import io.github.l2hyunwoo.compose.camera.core.AndroidCameraControllerContext
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraLens
import io.github.l2hyunwoo.compose.camera.core.FlashMode
import io.github.l2hyunwoo.compose.camera.core.ImageCaptureResult
import io.github.l2hyunwoo.compose.camera.core.initialize
import io.github.l2hyunwoo.compose.camera.core.surfaceRequestFlow
import kotlinx.coroutines.launch

/**
 * Sample activity demonstrating core-only usage of compose-camera.
 *
 * This example shows how to use the core module directly without the compose module,
 * using CameraXViewfinder for the preview and the DSL for controller configuration.
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
          CoreOnlyCameraScreen()
        }
      }
    }
  }
}

/**
 * Camera screen using core module only.
 *
 * Demonstrates:
 * - Creating CameraController with DSL
 * - Using CameraXViewfinder directly for preview
 * - Taking pictures with the controller
 */
@Composable
fun CoreOnlyCameraScreen() {
  val scope = rememberCoroutineScope()

  // Create controller using the DSL pattern
  val controller = rememberCoreOnlyController(
    configuration = CameraConfiguration(
      lens = CameraLens.BACK,
      flashMode = FlashMode.OFF,
    ),
  )

  var captureResult by remember { mutableStateOf<String?>(null) }

  // Initialize camera
  LaunchedEffect(controller) {
    controller.initialize()
  }

  // Cleanup on dispose
  DisposableEffect(controller) {
    onDispose {
      controller.release()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Custom preview using CameraXViewfinder directly
    CoreOnlyPreview(
      controller = controller,
      modifier = Modifier.fillMaxSize(),
    )

    // Capture button
    Button(
      onClick = {
        scope.launch {
          val result = controller.takePicture()
          captureResult = when (result) {
            is ImageCaptureResult.Success -> "Captured: ${result.width}x${result.height}"
            is ImageCaptureResult.Error -> "Error: ${result.exception.message}"
          }
        }
      },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(32.dp),
    ) {
      Text("Take Picture")
    }

    // Show capture result
    captureResult?.let { result ->
      Text(
        text = result,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(16.dp),
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

/**
 * Create and remember a CameraController for core-only usage.
 *
 * This demonstrates using AndroidCameraControllerContext for non-compose usage patterns.
 *
 * Note: The controller is created once with the initial configuration.
 * To update settings after creation, use [CameraController.updateConfiguration].
 */
@Composable
fun rememberCoreOnlyController(
  initialConfiguration: CameraConfiguration,
): CameraController {
  val context = androidx.compose.ui.platform.LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

  return remember {
    // Initialize the context holder for factory usage
    AndroidCameraControllerContext.initialize(context, lifecycleOwner)

    // Create controller directly
    AndroidCameraController(
      context = context,
      lifecycleOwner = lifecycleOwner,
      initialConfiguration = initialConfiguration,
    )
  }
}

/**
 * Custom preview composable using CameraXViewfinder directly.
 *
 * This shows how to build your own preview without using the compose module's CameraPreview.
 */
@Composable
fun CoreOnlyPreview(
  controller: CameraController,
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
