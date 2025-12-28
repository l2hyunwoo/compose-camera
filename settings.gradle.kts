pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "compose-camera"
include(":library:core")
include(":library:compose")
include(":plugin:mlkit-barcode")
include(":plugin:mlkit-text")
include(":sample")
