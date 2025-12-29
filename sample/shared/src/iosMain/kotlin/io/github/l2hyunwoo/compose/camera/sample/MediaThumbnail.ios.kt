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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * iOS implementation of media thumbnail.
 * TODO: Add actual image loading using PHImageManager
 */
@Composable
actual fun MediaThumbnailImage(
  item: MediaItem,
  modifier: Modifier,
) {
  Box(
    modifier = modifier.background(Color.DarkGray),
    contentAlignment = Alignment.Center,
  ) {
    // Placeholder - PHImageManager integration would go here
    Text(
      text = if (item.isVideo) "🎬" else "📷",
      fontSize = 24.sp,
    )

    Text(
      text = item.displayName.take(8),
      color = Color.White,
      fontSize = 10.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.BottomCenter).padding(4.dp),
    )

    if (item.isVideo) {
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
    }
  }
}
