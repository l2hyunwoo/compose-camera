import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.l2hyunwoo.camera.compose"
version = "1.0.0-alpha01"

kotlin {
    androidLibrary {
        namespace = "io.github.l2hyunwoo.camera.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":library:core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.camerax.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "camera-compose", version.toString())

    pom {
        name = "Compose Camera UI"
        description = "Compose UI components for Compose Camera library"
        inceptionYear = "2024"
        url = "https://github.com/l2hyunwoo/compose-camera/"
        licenses {
            license {
                name = "Apache License 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "l2hyunwoo"
                name = "Hyunwoo Lee"
                url = "https://github.com/l2hyunwoo"
            }
        }
        scm {
            url = "https://github.com/l2hyunwoo/compose-camera"
            connection = "scm:git:git://github.com/l2hyunwoo/compose-camera.git"
            developerConnection = "scm:git:ssh://git@github.com/l2hyunwoo/compose-camera.git"
        }
    }
}
