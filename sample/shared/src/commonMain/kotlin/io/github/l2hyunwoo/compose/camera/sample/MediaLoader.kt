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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

/**
 * Media item representing a captured photo or video.
 */
@Serializable
data class MediaItem(
  val uri: String,
  val isVideo: Boolean,
  val dateAdded: Long,
  val displayName: String,
  /** Width of the media in pixels */
  val width: Int,
  /** Height of the media in pixels */
  val height: Int,
)

/**
 * Platform-specific media loader for loading captured photos and videos.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MediaLoader {
  suspend fun loadMedia(): List<MediaItem>
}

/**
 * Create a platform-specific media loader.
 */
@Composable
expect fun rememberMediaLoader(): MediaLoader

/**
 * Platform-specific media thumbnail composable.
 */
@Composable
expect fun MediaThumbnailImage(
  item: MediaItem,
  modifier: Modifier = Modifier,
)
