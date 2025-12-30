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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Gallery screen showing captured photos and videos.
 */
@Composable
fun GalleryScreen(
  onBack: () -> Unit,
  onItemClick: (MediaItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val mediaLoader = rememberMediaLoader()
  var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    isLoading = true
    mediaItems = mediaLoader.loadMedia()
    isLoading = false
  }

  Scaffold(
    topBar = {
      @OptIn(ExperimentalMaterial3Api::class)
      CenterAlignedTopAppBar(
        title = { Text("Gallery (${mediaItems.size})") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              scope.launch {
                isLoading = true
                mediaItems = mediaLoader.loadMedia()
                isLoading = false
              }
            },
          ) {
            Icon(
              imageVector = Icons.Filled.Refresh,
              contentDescription = "Refresh",
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Color.Black,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White,
        ),
      )
    },
    containerColor = Color.Black,
  ) { paddingValues ->
    if (isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = Color.White)
      }
    } else if (mediaItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
            imageVector = Icons.Filled.ImageNotSupported,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp),
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No photos or videos yet",
            color = Color.Gray,
            textAlign = TextAlign.Center,
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(mediaItems) { item ->
          MediaThumbnail(
            item = item,
            onClick = { onItemClick(item) },
          )
        }
      }
    }
  }
}

@Composable
private fun MediaThumbnail(
  item: MediaItem,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .aspectRatio(1f)
      .fillMaxWidth()
      .clickable(onClick = onClick),
  ) {
    MediaThumbnailImage(
      item = item,
      modifier = Modifier.fillMaxSize(),
    )

    // Video indicator
    if (item.isVideo) {
      Icon(
        imageVector = Icons.Filled.PlayCircle,
        contentDescription = "Video",
        tint = Color.White.copy(alpha = 0.8f),
        modifier = Modifier
          .align(Alignment.Center)
          .size(32.dp),
      )
    }
  }
}
