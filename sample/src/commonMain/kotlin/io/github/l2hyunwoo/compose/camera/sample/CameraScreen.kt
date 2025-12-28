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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.camera.compose.*
import io.github.l2hyunwoo.camera.core.*
import kotlinx.coroutines.launch

/**
 * Sample camera screen demonstrating the Compose Camera library.
 */
@Composable
fun CameraScreen(
  onGalleryClick: () -> Unit = {},
  onBarcodeScannerClick: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var cameraController by remember { mutableStateOf<CameraController?>(null) }
  var cameraConfig by remember { mutableStateOf(CameraConfiguration()) }
  val cameraState by cameraController?.cameraState?.collectAsState()
    ?: remember { mutableStateOf<CameraState>(CameraState.Initializing) }

  val scope = rememberCoroutineScope()
  var lastCaptureResult by remember { mutableStateOf<String?>(null) }
  var currentRecording by remember { mutableStateOf<VideoRecording?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Black,
    content = { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
      ) {
        // Camera Preview
        CameraPreview(
          modifier = Modifier.fillMaxSize(),
          configuration = cameraConfig,
          onCameraControllerReady = { controller ->
            cameraController = controller
          },
        )

        // Status overlay
        when (val state = cameraState) {
          is CameraState.Initializing -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator(color = Color.White)
            }
          }
          is CameraState.Error -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = "Error",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                  text = state.exception.message ?: "Unknown error",
                  color = Color.White,
                  style = MaterialTheme.typography.bodyLarge,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
          is CameraState.Ready -> {
            // Ready - show controls
          }
        }

        // Top controls overlay
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
              ),
            )
            .statusBarsPadding()
            .padding(16.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          // Flash mode button
          IconButton(
            onClick = {
              val newFlashMode = when (cameraConfig.flashMode) {
                FlashMode.OFF -> FlashMode.ON
                FlashMode.ON -> FlashMode.AUTO
                FlashMode.AUTO -> FlashMode.OFF
                FlashMode.TORCH -> FlashMode.OFF
              }
              cameraConfig = cameraConfig.copy(flashMode = newFlashMode)
              cameraController?.setFlashMode(newFlashMode)
            },
          ) {
            val iconAndTint = when (cameraConfig.flashMode) {
              FlashMode.OFF -> Icons.Filled.FlashOff to Color.White
              FlashMode.ON -> Icons.Filled.FlashOn to Color.Yellow
              FlashMode.AUTO -> Icons.Filled.FlashAuto to Color.White
              FlashMode.TORCH -> Icons.Filled.Highlight to Color.Yellow
            }
            Icon(
              imageVector = iconAndTint.first,
              contentDescription = "Flash Mode",
              tint = iconAndTint.second,
            )
          }

          // Lens switch button
          IconButton(
            onClick = {
              val newLens = when (cameraConfig.lens) {
                CameraLens.BACK -> CameraLens.FRONT
                CameraLens.FRONT -> CameraLens.BACK
              }
              cameraConfig = cameraConfig.copy(lens = newLens)
              cameraController?.setLens(newLens)
            },
          ) {
            Icon(
              imageVector = Icons.Filled.Cameraswitch,
              contentDescription = "Switch Camera",
              tint = Color.White,
            )
          }

          // Barcode scanner button
          IconButton(onClick = onBarcodeScannerClick) {
            Icon(
              imageVector = Icons.Filled.QrCodeScanner,
              contentDescription = "Barcode Scanner",
              tint = Color.White,
            )
          }
        }

        // Capture result toast
        lastCaptureResult?.let { result ->
          LaunchedEffect(result) {
            kotlinx.coroutines.delay(3000)
            lastCaptureResult = null
          }

          Box(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 80.dp)
              .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.medium)
              .padding(horizontal = 16.dp, vertical = 8.dp),
          ) {
            Text(result, color = Color.White)
          }
        }

        // Bottom controls overlay
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
              brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
              ),
            )
            .navigationBarsPadding()
            .padding(bottom = 32.dp, top = 20.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            // Gallery button
            IconButton(
              onClick = onGalleryClick,
              modifier = Modifier.size(56.dp),
            ) {
              Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Gallery",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
              )
            }

            // Shutter Button (Video/Photo)
            val isRecording = (cameraState as? CameraState.Ready)?.isRecording == true
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .clickable(
                  onClick = {
                    scope.launch {
                      cameraController?.let { controller ->
                        when (val result = controller.takePicture()) {
                          is ImageCaptureResult.Success -> {
                            lastCaptureResult = "Captured ${result.width}x${result.height}"
                          }
                          is ImageCaptureResult.Error -> {
                            lastCaptureResult = "Error: ${result.exception.message}"
                          }
                        }
                      }
                    }
                  },
                ),
              // Long click for video recording could be added here
            ) {
              // Inner visualization
              Box(
                modifier = Modifier
                  .size(if (isRecording) 40.dp else 64.dp)
                  .clip(if (isRecording) MaterialTheme.shapes.small else CircleShape)
                  .background(if (isRecording) Color.Red else Color.White)
                  .animateContentSize(),
              )
            }
            // Separate Video Button for clarity in this sample
            IconButton(
              onClick = {
                scope.launch {
                  if (isRecording && currentRecording != null) {
                    // Stop recording
                    when (val result = currentRecording?.stop()) {
                      is VideoRecordingResult.Success -> {
                        lastCaptureResult = "Recorded ${result.durationMs / 1000}s"
                      }
                      is VideoRecordingResult.Error -> {
                        lastCaptureResult = "Error: ${result.exception.message}"
                      }
                      null -> {}
                    }
                    currentRecording = null
                  } else {
                    // Start recording
                    try {
                      currentRecording = cameraController?.startRecording()
                      lastCaptureResult = "Recording started..."
                    } catch (e: Exception) {
                      lastCaptureResult = "Error: ${e.message}"
                    }
                  }
                }
              },
              modifier = Modifier.size(56.dp),
            ) {
              Icon(
                imageVector = if (isRecording) Icons.Default.StopCircle else Icons.Default.Videocam,
                contentDescription = "Record Video",
                tint = if (isRecording) Color.Red else Color.White,
                modifier = Modifier.size(32.dp),
              )
            }
          }
        }
      }
    },
  )
}
