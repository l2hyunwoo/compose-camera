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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder

/**
 * Android implementation of media thumbnail using Coil.
 */
@Composable
actual fun MediaThumbnailImage(
  item: MediaItem,
  modifier: Modifier,
) {
  val context = LocalContext.current

  Box(modifier = modifier) {
    if (item.isVideo) {
      // Video thumbnail using Coil's VideoFrameDecoder
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(item.uri.toUri())
          .decoderFactory(VideoFrameDecoder.Factory())
          .build(),
        contentDescription = item.displayName,
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop,
      )

      // Video indicator badge
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(4.dp)
          .background(Color.Red, RoundedCornerShape(2.dp))
          .padding(horizontal = 4.dp, vertical = 2.dp),
      ) {
        Text(
          text = "VIDEO",
          color = Color.White,
          fontSize = 8.sp,
        )
      }
    } else {
      // Image thumbnail
      AsyncImage(
        model = item.uri.toUri(),
        contentDescription = item.displayName,
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.Crop,
      )
    }
  }
}
