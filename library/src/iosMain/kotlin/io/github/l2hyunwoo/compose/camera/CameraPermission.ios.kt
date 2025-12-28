package io.github.l2hyunwoo.compose.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume

/**
 * iOS implementation of [CameraPermissionManager].
 */
@OptIn(ExperimentalForeignApi::class)
actual class CameraPermissionManager {
    
    actual suspend fun checkPermission(permission: CameraPermission): PermissionStatus {
        return when (permission) {
            CameraPermission.CAMERA -> checkCameraPermission()
            CameraPermission.MICROPHONE -> checkMicrophonePermission()
            CameraPermission.STORAGE -> PermissionStatus.GRANTED // Not needed on iOS
        }
    }

    actual suspend fun requestPermission(permission: CameraPermission): PermissionStatus {
        return when (permission) {
            CameraPermission.CAMERA -> requestCameraPermission()
            CameraPermission.MICROPHONE -> requestMicrophonePermission()
            CameraPermission.STORAGE -> PermissionStatus.GRANTED // Not needed on iOS
        }
    }

    actual suspend fun requestCameraPermissions(): PermissionResult {
        val permissions = mutableMapOf<CameraPermission, PermissionStatus>()
        
        // Camera permission
        permissions[CameraPermission.CAMERA] = requestCameraPermission()
        
        // Microphone permission
        permissions[CameraPermission.MICROPHONE] = requestMicrophonePermission()
        
        return PermissionResult(permissions)
    }

    actual fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }

    private fun checkCameraPermission(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
            else -> PermissionStatus.DENIED
        }
    }

    private fun checkMicrophonePermission(): PermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)) {
            AVAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            AVAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
            else -> PermissionStatus.DENIED
        }
    }

    private suspend fun requestCameraPermission(): PermissionStatus {
        val currentStatus = checkCameraPermission()
        if (currentStatus != PermissionStatus.NOT_DETERMINED) {
            return currentStatus
        }
        
        return suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                continuation.resume(
                    if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
                )
            }
        }
    }

    private suspend fun requestMicrophonePermission(): PermissionStatus {
        val currentStatus = checkMicrophonePermission()
        if (currentStatus != PermissionStatus.NOT_DETERMINED) {
            return currentStatus
        }
        
        return suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { granted ->
                continuation.resume(
                    if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED
                )
            }
        }
    }
}

/**
 * Create iOS [CameraPermissionManager].
 */
@Composable
actual fun rememberCameraPermissionManager(): CameraPermissionManager {
    return remember { CameraPermissionManager() }
}
