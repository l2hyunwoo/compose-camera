# Migration Guide

This guide helps you migrate from older versions of compose-camera to the latest API.

## Migrating to Extensible Core API

### 1. rememberCameraController Migration

The `rememberCameraController` function has moved from the `core` module to the `compose` module.

**Before:**
```kotlin
import io.github.l2hyunwoo.compose.camera.core.rememberCameraController

@Composable
fun CameraScreen() {
    val controller = rememberCameraController(configuration)
}
```

**After:**
```kotlin
import io.github.l2hyunwoo.compose.camera.ui.rememberCameraController

@Composable
fun CameraScreen() {
    val controller = rememberCameraController(configuration)
}
```

The old import will show a deprecation warning. Simply update the import statement.

### 2. Focus Point Migration

`focus(Offset)` is deprecated in favor of `focus(FocusPoint)` for platform-agnostic focus control.

**Before:**
```kotlin
import androidx.compose.ui.geometry.Offset

// Using Compose Offset
cameraControl.focus(Offset(0.5f, 0.5f))
```

**After:**
```kotlin
import io.github.l2hyunwoo.compose.camera.core.FocusPoint

// Using platform-agnostic FocusPoint
cameraControl.focus(FocusPoint(0.5f, 0.5f))

// Or use the CENTER constant
cameraControl.focus(FocusPoint.CENTER)

// For tap coordinates from UI, use clamped() to ensure valid range
val focusPoint = FocusPoint.clamped(tapX / viewWidth, tapY / viewHeight)
cameraControl.focus(focusPoint)
```

**Compose Extension:**
If you're using Compose, convenience extensions are available:

```kotlin
import io.github.l2hyunwoo.compose.camera.ui.toFocusPoint
import io.github.l2hyunwoo.compose.camera.ui.toOffset

// Convert Offset to FocusPoint
val focusPoint = offset.toFocusPoint()

// Convert FocusPoint back to Offset
val offset = focusPoint.toOffset()
```

### 3. Using the New DSL for CameraController

The new DSL provides a cleaner way to configure the camera controller.

**Before (Configuration only):**
```kotlin
val controller = rememberCameraController(
    configuration = CameraConfiguration(
        lens = CameraLens.BACK,
        flashMode = FlashMode.AUTO
    )
)
```

**After (DSL with extensions and plugins):**
```kotlin
// Simple usage
val controller = CameraController()

// Full DSL configuration
val controller = CameraController {
    configuration = CameraConfiguration(
        lens = CameraLens.BACK,
        flashMode = FlashMode.AUTO
    )

    extensions {
        +ManualFocusExtension()
        +WhiteBalanceExtension()
    }

    plugins {
        +QRScannerPlugin()
    }

    imageCaptureUseCase = CustomCaptureUseCase()
}
```

### 4. Creating Custom Extensions

Extensions allow you to add custom camera controls without modifying the core library.

```kotlin
class ManualFocusExtension : CameraControlExtension {
    override val id = "manual-focus"
    private var controller: CameraController? = null

    var focusDistance: Float = 0f
        set(value) {
            field = value
            applyFocusDistance(value)
        }

    override fun onAttach(controller: CameraController) {
        this.controller = controller
    }

    override fun onDetach() {
        controller = null
    }

    override fun onCameraReady() {
        // Camera is initialized, safe to access hardware features
    }

    override fun onCameraReleased() {
        // Clean up before camera release
    }

    private fun applyFocusDistance(distance: Float) {
        // Platform-specific implementation
    }
}

// Usage
val controller = CameraController {
    extensions {
        +ManualFocusExtension()
    }
}

// Retrieve and use the extension
val focusExt = controller.getExtension<ManualFocusExtension>("manual-focus")
focusExt?.focusDistance = 0.5f
```

### 5. Custom Capture Use Cases

Replace the default capture behavior with custom implementations.

```kotlin
class RawCaptureUseCase : ImageCaptureUseCase {
    override suspend fun capture(
        controller: CameraController,
        config: CaptureConfig
    ): ImageCaptureResult {
        // Custom RAW capture implementation
        val androidController = controller as AndroidCameraController
        // ... capture logic
        return ImageCaptureResult.Success(bytes, width, height)
    }
}

// Usage
val controller = CameraController {
    imageCaptureUseCase = RawCaptureUseCase()
}

// Capture uses your custom implementation
val result = controller.takePicture()
```

### 6. Custom Preview Implementation

For advanced use cases, you can bypass `CameraPreview` and use the core library directly.

**Android:**
```kotlin
class CustomPreviewView(context: Context) : SurfaceView(context) {
    fun attachToController(controller: CameraController) {
        // Get the SurfaceRequest flow
        lifecycleScope.launch {
            controller.surfaceRequestFlow.collect { request ->
                request?.let { surfaceRequest ->
                    // Provide your surface to CameraX
                    surfaceRequest.provideSurface(
                        holder.surface,
                        executor
                    ) { result -> }
                }
            }
        }
    }
}
```

**iOS:**
```kotlin
// In Kotlin
val session = controller.captureSession

// In Swift
let previewLayer = AVCaptureVideoPreviewLayer(session: controller.captureSession)
previewLayer.videoGravity = .resizeAspectFill
view.layer.addSublayer(previewLayer)
```

## Compatibility Notes

- All deprecated APIs continue to work and will emit compiler warnings
- Binary compatibility is maintained for existing code
- The deprecation level is `WARNING`, not `ERROR`, so your existing code will compile

## Getting Help

If you encounter issues during migration, please:
1. Check the [API documentation](https://l2hyunwoo.github.io/compose-camera/)
2. Review the [sample applications](./sample/)
3. Open an issue on [GitHub](https://github.com/l2hyunwoo/compose-camera/issues)
