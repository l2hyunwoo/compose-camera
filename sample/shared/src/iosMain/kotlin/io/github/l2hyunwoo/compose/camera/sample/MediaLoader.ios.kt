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
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.Photos.*

/**
 * iOS implementation of MediaLoader using Photos framework.
 */
@OptIn(ExperimentalForeignApi::class)
actual class MediaLoader {

  actual suspend fun loadMedia(): List<MediaItem> {
    val mediaItems = mutableListOf<MediaItem>()

    // Request authorization first
    val status = PHPhotoLibrary.authorizationStatus()
    if (status != PHAuthorizationStatusAuthorized) {
      return emptyList()
    }

    // Fetch assets from the camera roll
    val fetchOptions = PHFetchOptions().apply {
      sortDescriptors = listOf(
        NSSortDescriptor.sortDescriptorWithKey("creationDate", ascending = false),
      )
    }

    val result = PHAsset.fetchAssetsWithOptions(fetchOptions)

    for (i in 0 until result.count.toInt().coerceAtMost(50)) {
      val asset = result.objectAtIndex(i.toULong()) as? PHAsset ?: continue
      val isVideo = asset.mediaType == PHAssetMediaTypeVideo
      val dateAdded = (asset.creationDate?.timeIntervalSince1970 ?: 0.0).toLong()

      mediaItems.add(
        MediaItem(
          uri = asset.localIdentifier,
          isVideo = isVideo,
          dateAdded = dateAdded,
          displayName = "Media_$i",
          width = asset.pixelWidth.toInt(),
          height = asset.pixelHeight.toInt(),
        ),
      )
    }

    return mediaItems
  }
}

@Composable
actual fun rememberMediaLoader(): MediaLoader = remember { MediaLoader() }
