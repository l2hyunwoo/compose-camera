# HDR Capture Plugin

HDR (High Dynamic Range) capture plugin for the `compose-camera` library. This plugin enables high-quality photo capture in challenging lighting conditions by leveraging platform-specific HDR capabilities.

## Implementations

- **Android**: Uses CameraX Extensions API (`ExtensionMode.HDR`). Requires device support for HDR extensions.
- **iOS**: Uses the system's default HDR capture pipeline (AVFoundation).

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
// build.gradle.kts (commonMain)
implementation("io.github.l2hyunwoo:camera-plugin-hdr:1.0.0")
```

If you are using Version Catalogs:

```toml
[libraries]
compose-camera-plugin-hdr = { module = "io.github.l2hyunwoo:camera-plugin-hdr", version.ref = "compose-camera" }
```

## Usage

Integrate the HDR plugin in your camera screen composable.

```kotlin
import io.github.l2hyunwoo.camera.plugin.hdr.rememberHDRCapture
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration

@Composable
fun CameraScreen() {
    // 1. Create and remember the HDR plugin instance
    // - On Android: This uses LocalContext.current internally
    // - On iOS: Creates a default instance
    val hdrPlugin = rememberHDRCapture()

    // 2. Add the plugin to your CameraConfiguration
    // Note: Pass this during initial setup or update configuration as needed
    var config by remember { 
        mutableStateOf(CameraConfiguration(plugins = listOf(hdrPlugin))) 
    }
    
    // ... CameraPreview ...
    
    // 3. Control HDR Mode (Optional UI)
    // You can allow users to toggle HDR if it's supported on the device
    val isHdrSupported by hdrPlugin.isSupported.collectAsState()
    
    if (isHdrSupported) {
        val isHdrEnabled by hdrPlugin.isEnabled.collectAsState()
        
        Button(onClick = { hdrPlugin.setEnabled(!isHdrEnabled) }) {
            Text(if (isHdrEnabled) "HDR ON" else "HDR OFF")
        }
    }
}
```

### API

#### `rememberHDRCapture(): HDRCapture`
Composable function to create an instance of the plugin.

#### `HDRCapture`
- `isSupported: StateFlow<Boolean>`: Whether HDR is supported on the current device (and camera).
- `isEnabled: StateFlow<Boolean>`: Current enabled state of HDR mode.
- `setEnabled(enabled: Boolean)`: Enable or disable HDR mode.
