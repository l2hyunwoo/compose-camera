# Core-Only Android Sample

This sample demonstrates how to use the `compose-camera` core module directly without the compose module's `CameraPreview` composable.

## Overview

This approach is useful when you need:
- Full control over the camera preview rendering
- Custom preview implementations using `CameraXViewfinder` directly
- Integration with existing camera UI frameworks

## Key Concepts

### 1. Direct Controller Creation

Instead of using `rememberCameraController` from the compose module, create the controller directly:

```kotlin
val controller = AndroidCameraController(
    context = context,
    lifecycleOwner = lifecycleOwner,
    initialConfiguration = CameraConfiguration(
        lens = CameraLens.BACK,
        flashMode = FlashMode.OFF,
    ),
)
```

### 2. Custom Preview with CameraXViewfinder

Use `surfaceRequestFlow` to get the `SurfaceRequest` and pass it to `CameraXViewfinder`:

```kotlin
@Composable
fun CoreOnlyPreview(controller: CameraController) {
    val surfaceRequest by controller.surfaceRequestFlow.collectAsState()

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
```

### 3. DSL Configuration (Optional)

You can also use the DSL pattern with `AndroidCameraControllerContext`:

```kotlin
// Initialize context first
AndroidCameraControllerContext.initialize(context, lifecycleOwner)

// Then use the DSL
val controller = CameraController {
    configuration = CameraConfiguration(lens = CameraLens.BACK)

    extensions {
        +ManualFocusExtension()
    }
}
```

## Running the Sample

```bash
./gradlew :sample:core-only-android:installDebug
```

## Dependencies

This sample only depends on:
- `:library:core` - The core camera library
- `androidx.camera:camera-compose` - For `CameraXViewfinder`
- Standard Compose UI dependencies
