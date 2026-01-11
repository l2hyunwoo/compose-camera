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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 *
 * This example shows how to use ManualFocusExtension to control
 * camera focus distance using Camera2 Interop APIs.
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
          ManualFocusScreen()
        }
      }
    }
  }
}

@Composable
fun ManualFocusScreen() {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // Create manual focus extension
  val manualFocusExtension = remember { ManualFocusExtension() }

  // Create controller and register extension
  val controller = remember {
    AndroidCameraController(
      context = context,
      lifecycleOwner = lifecycleOwner,
      initialConfiguration = CameraConfiguration(lens = CameraLens.BACK),
    ).also { ctrl ->
      ctrl.registerExtension(manualFocusExtension)
    }
  }

  // Observe extension states
  val isManualFocusEnabled by manualFocusExtension.isManualFocusEnabled.collectAsState()
  val isSupported by manualFocusExtension.isSupported.collectAsState()
  val focusDistance by manualFocusExtension.focusDistance.collectAsState()
  val minFocusDistance by manualFocusExtension.minFocusDistance.collectAsState()

  // Local slider state
  var sliderValue by remember { mutableFloatStateOf(0f) }

  // Sync slider with focus distance
  LaunchedEffect(focusDistance, minFocusDistance) {
    if (minFocusDistance > 0f) {
      sliderValue = focusDistance / minFocusDistance
    }
  }

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
    CameraPreview(
      controller = controller,
      modifier = Modifier.fillMaxSize(),
    )

    // Top info overlay
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
          ),
        )
        .statusBarsPadding()
        .padding(16.dp),
    ) {
      Text(
        text = "Manual Focus Extension",
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (isSupported) {
          "Manual focus supported on this device"
        } else {
          "Manual focus NOT supported on this device"
        },
        color = if (isSupported) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
        style = MaterialTheme.typography.bodySmall,
      )

      if (isSupported) {
        Text(
          text = "Min focus distance: ${String.format("%.2f", minFocusDistance)} diopters",
          color = Color.White.copy(alpha = 0.7f),
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }

    // Bottom control panel
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
          ),
        )
        .navigationBarsPadding()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Manual focus toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color.White.copy(alpha = 0.1f))
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Manual Focus",
            color = Color.White,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = if (isManualFocusEnabled) "Enabled" else "Auto",
            color = if (isManualFocusEnabled) Color.Yellow else Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall,
          )
        }

        Switch(
          checked = isManualFocusEnabled,
          onCheckedChange = { enabled ->
            if (enabled) {
              manualFocusExtension.enableManualFocus()
            } else {
              manualFocusExtension.disableManualFocus()
            }
          },
          enabled = isSupported,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.Yellow,
            checkedTrackColor = Color.Yellow.copy(alpha = 0.5f),
          ),
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Focus distance slider
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isManualFocusEnabled) {
              Color.White.copy(alpha = 0.15f)
            } else {
              Color.White.copy(alpha = 0.05f)
            },
          )
          .padding(16.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Focus Distance",
            color = if (isManualFocusEnabled) Color.White else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = String.format("%.2f", focusDistance) + " diopters",
            color = if (isManualFocusEnabled) Color.Yellow else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Distance labels
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "∞ Far",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
          )
          Text(
            text = "Near (Macro)",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
          )
        }

        Slider(
          value = sliderValue,
          onValueChange = { newValue ->
            sliderValue = newValue
            manualFocusExtension.setNormalizedFocusDistance(newValue)
          },
          valueRange = 0f..1f,
          enabled = isManualFocusEnabled,
          colors = SliderDefaults.colors(
            thumbColor = Color.Yellow,
            activeTrackColor = Color.Yellow,
            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
            disabledThumbColor = Color.Gray,
            disabledActiveTrackColor = Color.Gray.copy(alpha = 0.5f),
            disabledInactiveTrackColor = Color.Gray.copy(alpha = 0.2f),
          ),
          modifier = Modifier.fillMaxWidth(),
        )

        // Visual focus indicator
        if (isManualFocusEnabled) {
          Spacer(modifier = Modifier.height(8.dp))
          FocusDistanceIndicator(
            normalizedDistance = sliderValue,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Hint text
      Text(
        text = if (isManualFocusEnabled) {
          "Slide to adjust focus. Left = far, Right = near"
        } else {
          "Enable manual focus to control focus distance"
        },
        color = Color.White.copy(alpha = 0.5f),
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
fun FocusDistanceIndicator(
  normalizedDistance: Float,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Approximate distance in meters (rough estimation)
    val distanceText = when {
      normalizedDistance < 0.1f -> "∞"
      normalizedDistance < 0.3f -> "~3m"
      normalizedDistance < 0.5f -> "~1m"
      normalizedDistance < 0.7f -> "~50cm"
      normalizedDistance < 0.9f -> "~20cm"
      else -> "~10cm"
    }

    Text(
      text = "Approx. distance: $distanceText",
      color = Color.Cyan,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
fun CameraPreview(
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
