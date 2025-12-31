pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "compose-camera"
include(":library:core")
include(":library:compose")
include(":plugin:mlkit-barcode")
include(":plugin:mlkit-text")
include(":sample:shared")
include(":sample:android-app")
