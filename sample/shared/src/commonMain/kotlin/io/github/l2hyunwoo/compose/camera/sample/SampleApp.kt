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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import io.github.l2hyunwoo.compose.camera.core.rememberCameraPermissionManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.typeOf

/**
 * Sample app entry point composable.
 * Handles permission requests and displays camera/gallery screens.
 */
@Composable
fun SampleApp() {
  val permissionManager = rememberCameraPermissionManager()
  var hasPermission by remember { mutableStateOf(false) }
  var permissionChecked by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val result = permissionManager.requestCameraPermissions()
    hasPermission = result.cameraGranted
    permissionChecked = true
  }

  MaterialTheme(
    colorScheme = darkColorScheme(),
  ) {
    Box(
      modifier = Modifier.applyTestTagsAsResourceId(),
    ) {
      if (!permissionChecked) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Checking permissions...",
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag("permission_checking"),
          )
        }
      } else if (hasPermission) {
        val navController = rememberNavController()

        NavHost(
          navController = navController,
          startDestination = Destination.Camera,
        ) {
          composable<Destination.Camera> {
            CameraScreen(
              onGalleryClick = { navController.navigate(Destination.Gallery) },
              onBarcodeScannerClick = { navController.navigate(Destination.BarcodeScanner) },
            )
          }

          composable<Destination.Gallery> {
            GalleryScreen(
              onBack = { navController.popBackStack() },
              onItemClick = { item -> navController.navigate(Destination.MediaDetail(item)) },
            )
          }

          composable<Destination.MediaDetail>(
            typeMap = mapOf(typeOf<MediaItem>() to MediaItemNavType),
          ) { backStackEntry ->
            val detail = backStackEntry.toRoute<Destination.MediaDetail>()
            MediaDetailScreen(
              item = detail.item,
              onBack = { navController.popBackStack() },
            )
          }

          composable<Destination.BarcodeScanner> {
            BarcodeScannerScreen(
              onBack = { navController.popBackStack() },
            )
          }
        }
      } else {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Button(
            onClick = { permissionManager.openAppSettings() },
            modifier = Modifier.testTag("permission_settings_button"),
          ) {
            Text("Open Camera Permission Settings")
          }
        }
      }
    }
  }
}

private sealed class Destination {
  @Serializable
  data object Camera : Destination()

  @Serializable
  data object Gallery : Destination()

  @Serializable
  data object BarcodeScanner : Destination()

  @Serializable
  data class MediaDetail(val item: MediaItem) : Destination()
}

private val MediaItemNavType = object : NavType<MediaItem>(isNullableAllowed = false) {
  override fun get(bundle: SavedState, key: String): MediaItem? = bundle.read {
    if (contains(key)) getString(key)?.let { Json.decodeFromString(it) } else null
  }

  @OptIn(ExperimentalEncodingApi::class)
  override fun parseValue(value: String): MediaItem = Json.decodeFromString(Base64.decode(value).decodeToString())

  override fun put(bundle: SavedState, key: String, value: MediaItem) {
    bundle.write {
      putString(key, Json.encodeToString(value))
    }
  }

  @OptIn(ExperimentalEncodingApi::class)
  override fun serializeAsValue(value: MediaItem): String = Base64.encode(Json.encodeToString(value).encodeToByteArray())
}
