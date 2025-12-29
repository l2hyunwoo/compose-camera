# Compose Camera

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blueviolet.svg?style=flat)](https://www.jetbrains.com/lp/compose-multiplatform/)

A robust, feature-rich camera library for Compose Multiplatform supporting Android and iOS. Built with CameraX on Android and AVFoundation on iOS.

## Features

- 📱 **Cross-Platform**: Unified API for Android and iOS
- 📸 **Camera Preview**: High-performance camera preview using native views
- 🖼️ **Image Capture**: Capture high-quality photos with flash support
- 🎥 **Video Recording**: Record videos with audio
- 🔄 **Lens Control**: Switch between Front and Back cameras
- 🔦 **Flash Control**: Torch, On, Off, Auto modes
- 🔍 **Pinch Zoom**: Native pinch-to-zoom gesture and zoom ratio control
- ✋ **Permission Handling**: Built-in, platform-independent permission manager
- 🧩 **Plugin Architecture**: Extensible design for frame processing and custom features

| Platform | Status | Implementation |
|----------|--------|----------------|
| Android  | ✅ Ready | CameraX + CameraXViewfinder (Compose) |
| iOS      | ✅ Ready | AVFoundation + UIKitView |

## Installation

Add the following dependencies to your `build.gradle.kts`:

### Gradle (Version Catalog)

```toml
# libs.versions.toml
[versions]
compose-camera = "1.0.0"

[libraries]
compose-camera-core = { module = "io.github.l2hyunwoo:compose-camera-core", version.ref = "compose-camera" }
compose-camera-ui = { module = "io.github.l2hyunwoo:compose-camera-ui", version.ref = "compose-camera" }
```

```kotlin
// build.gradle.kts (commonMain)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.camera.core)
            implementation(libs.compose.camera.ui)
        }
    }
}
```

### Gradle (Direct)

```kotlin
// build.gradle.kts (commonMain)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.l2hyunwoo:compose-camera-core:1.0.0")
            implementation("io.github.l2hyunwoo:compose-camera-ui:1.0.0")
        }
    }
}
```

> 📦 **Modules**:
> - `compose-camera-core`: Core camera logic, controllers, and models
> - `compose-camera-ui`: Compose UI components (CameraPreview)

## Setup

### Android (`AndroidManifest.xml`)

Add necessary permissions:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<!-- For saving to gallery on older Android versions -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
```

### iOS (`Info.plist`)

Add usage descriptions and high-refresh rate support:

```xml
<key>NSCameraUsageDescription</key>
<string>This app needs camera access to capture photos.</string>
<key>NSMicrophoneUsageDescription</key>
<string>This app needs microphone access to record videos.</string>

<!-- Important for smooth preview on iPhone Pro models -->
<key>CADisableMinimumFrameDurationOnPhone</key>
<true/>
```

## Usage

### 1. Permission Handling

Use the platform-independent `rememberCameraPermissionManager` to handle permissions easily.

```kotlin
import io.github.l2hyunwoo.compose.camera.core.rememberCameraPermissionManager

@Composable
fun CameraScreen() {
  val permissionManager = rememberCameraPermissionManager()
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    val result = permissionManager.requestCameraPermissions()
    if (result.cameraGranted) {
      // Permission granted
    } else {
      // Permission denied
    }
  }
}
```

### 2. Camera Preview & Controls

```kotlin
import io.github.l2hyunwoo.compose.camera.core.*
import io.github.l2hyunwoo.compose.camera.ui.CameraPreview

@Composable
fun MyCameraScreen() {
  var cameraController by remember { mutableStateOf<CameraController?>(null) }
  var config by remember { mutableStateOf(CameraConfiguration()) }

  Box(modifier = Modifier.fillMaxSize()) {
    CameraPreview(
      modifier = Modifier.fillMaxSize(),
      configuration = config,
      onCameraControllerReady = { controller ->
        cameraController = controller
      },
    )

    // Example Controls
    Button(
      onClick = {
        val newLens = if (config.lens == CameraLens.BACK) CameraLens.FRONT else CameraLens.BACK
        config = config.copy(lens = newLens)
        cameraController?.setLens(newLens)
      },
    ) {
      Text("Switch Camera")
    }

    Button(
      onClick = {
        scope.launch {
          when (val result = cameraController?.takePicture()) {
            is ImageCaptureResult.Success -> {
              println("Image saved: ${result.filePath}")
            }
            is ImageCaptureResult.Error -> {
              println("Error: ${result.exception}")
            }
            else -> {}
          }
        }
      },
    ) {
      Text("Capture")
    }
  }
}
```


### 3. Video Recording

```kotlin
// Start Recording
val recording = cameraController?.startRecording()

// Stop Recording
scope.launch {
  val result = recording?.stop()
  when (result) {
    is VideoRecordingResult.Success -> {
      println("Video saved to: ${result.uri}")
    }
    else -> { /* Handle error */ }
  }
}
```


## License

```
Copyright 2025 l2hyunwoo

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
