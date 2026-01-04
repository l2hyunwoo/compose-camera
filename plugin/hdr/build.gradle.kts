/*
 * Copyright (C) 2025 l2hyunwoo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kmpNativeCoroutines)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.binaryCompatibilityValidator)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.l2hyunwoo"
version = "1.0.0"

kotlin {
    androidLibrary {
        namespace = "io.github.l2hyunwoo.camera.plugin.hdr"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

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
            api(project(":library:core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kmp.nativecoroutines.annotations)
            implementation(compose.runtime)
        }

        androidMain.dependencies {
            implementation(libs.camerax.core)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.extensions)
            implementation(libs.kotlinx.coroutines.guava)
            implementation(compose.ui)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "camera-plugin-hdr", version.toString())

    pom {
        name = "Compose Camera HDR Plugin"
        description = "HDR (High Dynamic Range) capture plugin for Compose Camera library"
        inceptionYear = "2025"
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
