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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.compose.camera.ui.CameraPreview
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.camera.plugin.mlkit.barcode.Barcode
import io.github.l2hyunwoo.camera.plugin.mlkit.barcode.BarcodeScanner

/**
 * Sample screen demonstrating the BarcodeScanner plugin.
 * Shows camera preview with detected barcodes overlaid at the bottom.
 */
@Composable
fun BarcodeScannerScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var cameraController by remember { mutableStateOf<CameraController?>(null) }
  val barcodeScanner = remember { BarcodeScanner() }
  val cameraConfig = remember {
    CameraConfiguration().withPlugin(barcodeScanner)
  }

  val barcodes by barcodeScanner.barcodes.collectAsState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = Color.Black,
  ) { paddingValues ->
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

      // Top bar with back button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent),
            ),
          )
          .statusBarsPadding()
          .padding(16.dp)
          .align(Alignment.TopCenter),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
          )
        }
        Text(
          text = "바코드 스캐너",
          style = MaterialTheme.typography.titleLarge,
          color = Color.White,
        )
      }

      // Barcode results overlay at the bottom
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
            ),
          )
          .navigationBarsPadding()
          .padding(16.dp),
      ) {
        if (barcodes.isEmpty()) {
          Text(
            text = "바코드를 카메라에 비춰주세요",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
          )
        } else {
          Text(
            text = "감지된 바코드: ${barcodes.size}개",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
          )
          Spacer(modifier = Modifier.height(8.dp))
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(150.dp),
          ) {
            items(barcodes) { barcode ->
              BarcodeCard(barcode = barcode)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BarcodeCard(barcode: Barcode) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = Color.White.copy(alpha = 0.15f),
    ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
    ) {
      Text(
        text = barcode.displayValue ?: barcode.rawValue,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White,
      )
      Text(
        text = "형식: ${barcode.format.name}",
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.7f),
      )
    }
  }
}
