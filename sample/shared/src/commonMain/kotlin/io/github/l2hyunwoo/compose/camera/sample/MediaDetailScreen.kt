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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Screen showing details of a specific media item.
 */
@Composable
fun MediaDetailScreen(
  item: MediaItem,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    topBar = {
      @OptIn(ExperimentalMaterial3Api::class)
      CenterAlignedTopAppBar(
        title = { Text("Media Detail") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color.Black,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
        ),
      )
    },
    containerColor = Color.Black,
  ) { paddingValues ->
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(Color.DarkGray),
        contentAlignment = Alignment.Center,
      ) {
        MediaThumbnailImage(
          item = item,
          modifier = Modifier.fillMaxSize(),
        )
      }

      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
        ) {
          DetailRow(label = "Name", value = item.displayName)
          DetailRow(label = "Resolution", value = "${item.width} x ${item.height}")
          val megapixels = (item.width * item.height) / 1_000_000f
          val megapixelsText = (kotlin.math.round(megapixels * 100) / 100.0).toString()
          DetailRow(label = "Megapixels", value = "$megapixelsText MP")
          DetailRow(label = "Type", value = if (item.isVideo) "Video" else "Image")
        }
      }
    }
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}
