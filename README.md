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
- ☀️ **Exposure Compensation**: Manual exposure adjustment (EV control)
- ✋ **Permission Handling**: Built-in, platform-independent permission manager
- 🎯 **Tap-to-Focus**: Auto focus on tapped area with customizable visual indicator
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
compose-camera = "1.2.2"

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
            implementation("io.github.l2hyunwoo:compose-camera-core:1.2.2")
            implementation("io.github.l2hyunwoo:compose-camera-ui:1.2.2")
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
      // Customize the focus indicator (Optional)
      focusIndicator = { tapPosition ->
        DefaultFocusIndicator(
          tapPosition = tapPosition,
          color = Color.Yellow,
          size = 64.dp,
        )
      }
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


### 4. Exposure Compensation

```kotlin
// Get exposure compensation range
val (minEV, maxEV) = cameraController?.exposureCompensationRange ?: Pair(-2f, 2f)

// Observe current exposure value
val currentEV by cameraController?.exposureCompensationFlow?.collectAsState() ?: remember { mutableStateOf(0f) }

// Set exposure compensation (in EV units)
cameraController?.setExposureCompensation(1.5f) // Brighten
cameraController?.setExposureCompensation(-1.0f) // Darken
```

## Advanced Usage

### Custom Preview (Core-Only)

For advanced use cases where you need full control over the preview, you can use the core module directly without the compose UI module.

**Android with CameraXViewfinder:**

```kotlin
// Required imports
import io.github.l2hyunwoo.compose.camera.core.*
import androidx.camera.compose.CameraXViewfinder // from androidx.camera:camera-compose artifact
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun CustomPreview(controller: CameraController, modifier: Modifier = Modifier) {
    // Access the SurfaceRequest flow from the controller
    val surfaceRequest by controller.surfaceRequestFlow.collectAsState()

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier,
        )
    }
}
```

**iOS with AVCaptureVideoPreviewLayer:**

```kotlin
// Required imports (iOS/Swift interop)
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill

// Access the capture session directly
val previewLayer = AVCaptureVideoPreviewLayer(session = controller.captureSession)
previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
view.layer.addSublayer(previewLayer)
```

### DSL Configuration

Create camera controllers with a clean, declarative DSL syntax:

```kotlin
import io.github.l2hyunwoo.compose.camera.core.*
import io.github.l2hyunwoo.compose.camera.ui.rememberCameraController

// In Compose: Use rememberCameraController with DSL
@Composable
fun CameraScreen() {
    val controller = rememberCameraController {
        configuration = CameraConfiguration(lens = CameraLens.BACK)

        extensions {
            +ExposureLockExtension()
        }

        plugins {
            +QRScannerPlugin()
        }
    }

    LaunchedEffect(controller) {
        controller.initialize()
    }
}

// Outside Compose: Initialize context first, then use CameraController factory
// Note: AndroidCameraControllerContext.initialize(context, lifecycleOwner) must be called first on Android
val simpleController = CameraController()

// Full DSL configuration
val configuredController = CameraController {
    // Set camera configuration
    configuration = CameraConfiguration(
        lens = CameraLens.BACK,
        flashMode = FlashMode.AUTO,
    )

    // Register extensions
    extensions {
        +ExposureLockExtension()
        +ManualFocusExtension()
    }

    // Register plugins for frame processing
    plugins {
        +QRScannerPlugin()
        +TextRecognitionPlugin()
    }

    // Custom capture implementations (optional)
    imageCaptureUseCase = CustomImageCaptureUseCase()
    videoCaptureUseCase = CustomVideoCaptureUseCase()
}

// Initialize and use
configuredController.initialize()
```

### Extending Controls

Create custom camera control extensions by implementing `CameraControlExtension`:

```kotlin
import io.github.l2hyunwoo.compose.camera.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExposureLockExtension : CameraControlExtension {
    override val id: String = "exposure-lock"

    private var controller: CameraController? = null
    private var lockedExposureValue: Float? = null

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    override fun onAttach(controller: CameraController) {
        this.controller = controller
    }

    override fun onDetach() {
        controller = null
        _isLocked.value = false
    }

    override fun onCameraReady() {
        // Camera is ready, can access hardware features
    }

    override fun onCameraReleased() {
        // Clean up before camera release
        lockedExposureValue = null
    }

    fun lock() {
        controller?.let { ctrl ->
            lockedExposureValue = ctrl.cameraInfo.exposureState.value.exposureCompensation
            _isLocked.value = true
        }
    }

    fun unlock() {
        lockedExposureValue = null
        _isLocked.value = false
    }

    fun toggle() {
        if (_isLocked.value) unlock() else lock()
    }
}
```

**Using Extensions:**

```kotlin
// Create extension instance
val exposureLock = ExposureLockExtension()

// Register with controller
controller.registerExtension(exposureLock)

// Or use DSL
val controller = CameraController {
    extensions {
        +exposureLock
    }
}

// Use the extension
exposureLock.lock()
val isLocked by exposureLock.isLocked.collectAsState()

// Retrieve extension from controller
val ext = controller.getExtension<ExposureLockExtension>()

// Unregister when done
controller.unregisterExtension(exposureLock)
```

### Custom Capture Use Cases

Implement custom image or video capture logic:

```kotlin
import io.github.l2hyunwoo.compose.camera.core.*

class BurstCaptureUseCase : ImageCaptureUseCase {
    override suspend fun capture(config: CaptureConfig?): ImageCaptureResult {
        // Custom burst capture implementation
        // Access platform-specific APIs as needed
        return ImageCaptureResult.Success(
            filePath = outputPath,
            width = width,
            height = height
        )
    }
}

// Use custom capture
val controller = CameraController {
    imageCaptureUseCase = BurstCaptureUseCase()
}
```

## Sample Applications

Explore the sample applications for complete implementation examples:

| Sample | Description |
|--------|-------------|
| [android-app](sample/android-app/) | Basic camera app with all features |
| [core-only-android](sample/core-only-android/) | Core module usage without compose UI |
| [custom-extensions](sample/custom-extensions/) | Custom extension implementation examples |

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
