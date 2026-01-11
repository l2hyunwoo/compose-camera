# Custom Extensions Sample

This sample demonstrates how to create and use custom `CameraControlExtension` implementations.

## Overview

Extensions allow you to add custom camera controls without modifying the core library. This sample includes two example extensions:

### ExposureLockExtension

Locks the current exposure value, preventing automatic exposure adjustments:

```kotlin
class ExposureLockExtension : CameraControlExtension {
    override val id = "exposure-lock"

    val isLocked: StateFlow<Boolean>

    fun lock()
    fun unlock()
    fun toggle()
}
```

### TapCounterExtension

Tracks tap-to-focus events for analytics:

```kotlin
class TapCounterExtension : CameraControlExtension {
    override val id = "tap-counter"

    val tapCount: StateFlow<Int>
    val focusHistory: StateFlow<List<FocusPoint>>

    fun recordTap(focusPoint: FocusPoint)
    fun reset()
}
```

## Usage

### Registering Extensions

```kotlin
val exposureLockExtension = ExposureLockExtension()
val tapCounterExtension = TapCounterExtension()

val controller = AndroidCameraController(
    context = context,
    lifecycleOwner = lifecycleOwner,
    initialConfiguration = CameraConfiguration(),
).also { ctrl ->
    ctrl.registerExtension(exposureLockExtension)
    ctrl.registerExtension(tapCounterExtension)
}
```

### Using Extensions

```kotlin
// Toggle exposure lock
exposureLockExtension.toggle()

// Check lock state
val isLocked by exposureLockExtension.isLocked.collectAsState()

// Record tap events
tapCounterExtension.recordTap(FocusPoint(0.5f, 0.5f))

// Get tap count
val tapCount by tapCounterExtension.tapCount.collectAsState()
```

### Retrieving Extensions

```kotlin
val extension = controller.getExtension<ExposureLockExtension>("exposure-lock")
extension?.lock()
```

## Extension Lifecycle

Extensions receive lifecycle callbacks:

- `onAttach(controller)` - Called when registered
- `onCameraReady()` - Called after camera initialization
- `onCameraReleased()` - Called before camera release
- `onDetach()` - Called when unregistered

## Running the Sample

```bash
./gradlew :sample:custom-extensions:installDebug
```
