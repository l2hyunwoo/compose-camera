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
package io.github.l2hyunwoo.compose.camera.sample

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PermissionFlowTest {

  private lateinit var device: UiDevice

  companion object {
    private const val PACKAGE = "io.github.l2hyunwoo.compose.camera.sample"
    private const val TIMEOUT = 10_000L

    // Exact resource IDs from Samsung Android 15 permission dialog
    private const val ALLOW_BUTTON_ID =
      "com.android.permissioncontroller:id/permission_allow_foreground_only_button"
    private const val DENY_BUTTON_ID = "com.android.permissioncontroller:id/permission_deny_button"
  }

  @Before
  fun setUp() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    device = UiDevice.getInstance(instrumentation)
    instrumentation.uiAutomation.revokeRuntimePermission(PACKAGE, Manifest.permission.CAMERA)
    instrumentation.uiAutomation.revokeRuntimePermission(PACKAGE, Manifest.permission.RECORD_AUDIO)
    Thread.sleep(500)
  }

  @Test
  fun test1_permissionDenied_showsSettingsButton() {
    launchApp()
    denyPermission()
    denyPermission()

    // Compose testTag with testTagsAsResourceId=true uses format: package:id/tag
    val settingsButton = device.wait(
      Until.findObject(By.res(PACKAGE, "permission_settings_button")),
      TIMEOUT,
    ) ?: device.findObject(By.textContains("Open Camera Permission Settings"))

    assert(settingsButton != null) { "Settings button should appear after permission denial" }
  }

  @Test
  fun test2_settingsButton_opensAppSettings() {
    launchApp()
    denyPermission()
    denyPermission()

    val settingsButton = device.wait(
      Until.findObject(By.res(PACKAGE, "permission_settings_button")),
      TIMEOUT,
    ) ?: device.findObject(By.textContains("Open Camera Permission Settings"))

    assert(settingsButton != null) { "Settings button should be found" }
    settingsButton?.click()
    Thread.sleep(1000) // Wait for settings to launch

    // Check for Samsung or AOSP settings package
    val inSettings = device.wait(
      Until.hasObject(By.pkg("com.android.settings")),
      TIMEOUT,
    ) || device.wait(
      Until.hasObject(By.pkg("com.samsung.android.settings")),
      TIMEOUT / 2,
    )
    assert(inSettings) { "App settings should open when settings button is clicked" }
  }

  @Test
  fun test3_permissionGranted_showsCameraScreen() {
    launchApp()
    grantPermission()
    grantPermission() // RECORD_AUDIO

    val cameraScreen = device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT)
    assert(cameraScreen) { "Camera screen should be displayed after permission grant" }
  }

  private fun launchApp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE)?.apply {
      addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
      addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    context.startActivity(intent)
    device.wait(Until.hasObject(By.pkg(PACKAGE)), TIMEOUT)
  }

  private fun grantPermission() {
    if (!device.wait(
        Until.hasObject(By.pkg("com.google.android.permissioncontroller")),
        TIMEOUT,
      )
    ) {
      return
    }

    // Try common allow button patterns
    val allowButton = device.findObject(By.res(ALLOW_BUTTON_ID))
      ?: device.findObject(By.textContains("허용"))
      ?: device.findObject(By.textContains("Allow"))
    allowButton?.click()
    Thread.sleep(300)
  }

  private fun denyPermission() {
    if (!device.wait(
        Until.hasObject(By.pkg("com.google.android.permissioncontroller")),
        TIMEOUT,
      )
    ) {
      return
    }

    val denyButton = device.findObject(By.res(DENY_BUTTON_ID))
      ?: device.findObject(By.textContains("허용 안함"))
      ?: device.findObject(By.textContains("Don't allow"))
    denyButton?.click()
    Thread.sleep(300)
  }
}
