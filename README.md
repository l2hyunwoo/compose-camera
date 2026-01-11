![Compose Camera Banner](assets/banner.jpg)

# Compose Camera

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.9.3-blueviolet.svg?style=flat)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Documentation](https://img.shields.io/badge/Documentation-Read%20Now-green)](https://l2hyunwoo.github.io/compose-camera)

A robust, feature-rich camera library for Compose Multiplatform supporting Android and iOS. Built with CameraX on Android and AVFoundation on iOS.

## 📚 Documentation

**[Read the full documentation here](https://l2hyunwoo.github.io/compose-camera)**

- [Quick Start](https://l2hyunwoo.github.io/compose-camera/docs/quick-start)
- [Architecture](https://l2hyunwoo.github.io/compose-camera/docs/architecture)
- [API Reference](https://l2hyunwoo.github.io/compose-camera/docs/api/core)

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

## Installation

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

For detailed setup instructions (AndroidManifest, Info.plist), please refer to the [Quick Start Guide](https://l2hyunwoo.github.io/compose-camera/docs/quick-start).

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
