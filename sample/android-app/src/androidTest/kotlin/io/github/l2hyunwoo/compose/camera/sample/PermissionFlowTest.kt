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
import android.app.UiAutomation
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
@RunWith(AndroidJUnit4::class)
class PermissionFlowTest {

  private lateinit var device: UiDevice

  companion object {
    private const val PACKAGE = "io.github.l2hyunwoo.compose.camera.sample"
    private const val TIMEOUT = 10_000L
    
    // Exact resource IDs from Samsung Android 15 permission dialog
    private const val ALLOW_BUTTON_ID = "com.android.permissioncontroller:id/permission_allow_foreground_only_button"
    private const val DENY_BUTTON_ID = "com.android.permissioncontroller:id/permission_deny_button"
  }

  @Before
  fun setUp() {
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
  }

  @Test
  fun permissionFlow() {
    // 1. Prepare: Revoke permissions
    revokePermissions()

    // 2. Deny Flow & Settings Button Check
    launchApp()
    denyPermission()
    denyPermission()

    val settingsButton = device.wait(
      Until.findObject(By.res(PACKAGE, "permission_settings_button")),
      TIMEOUT
    ) ?: device.findObject(By.textContains("Open Camera Permission Settings"))
    
    assert(settingsButton != null) { "Settings button should appear after permission denial" }
    
    // 3. Settings Navigation Check
    settingsButton?.click()
    Thread.sleep(1000)
    
    val inSettings = device.wait(
      Until.hasObject(By.pkg("com.android.settings")), 
      TIMEOUT
    ) || device.wait(
      Until.hasObject(By.pkg("com.samsung.android.settings")),
      TIMEOUT / 2
    )
    assert(inSettings) { "App settings should open when settings button is clicked" }

    // 4. Reset & Grant Flow
    device.pressHome()
    revokePermissions() // Reset state for grant test
    
    launchApp()
    grantPermission()
    grantPermission() // RECORD_AUDIO

    val cameraScreen = device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT)
    assert(cameraScreen) { "Camera screen should be displayed after permission grant" }
  }

  private fun revokePermissions() {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.uiAutomation.revokeRuntimePermission(PACKAGE, Manifest.permission.CAMERA)
    instrumentation.uiAutomation.revokeRuntimePermission(PACKAGE, Manifest.permission.RECORD_AUDIO)
    Thread.sleep(500)
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
    if (!device.wait(Until.hasObject(By.pkg("com.google.android.permissioncontroller")), TIMEOUT)) return
    
    // Try common allow button patterns
    val allowButton = device.findObject(By.res(ALLOW_BUTTON_ID))
      ?: device.findObject(By.textContains("허용"))
      ?: device.findObject(By.textContains("Allow"))
    allowButton?.click()
    Thread.sleep(300)
  }

  private fun denyPermission() {
    if (!device.wait(Until.hasObject(By.pkg("com.google.android.permissioncontroller")), TIMEOUT)) return
    
    val denyButton = device.findObject(By.res(DENY_BUTTON_ID))
      ?: device.findObject(By.textContains("허용 안함"))
      ?: device.findObject(By.textContains("Don't allow"))
    denyButton?.click()
    Thread.sleep(300)
  }
}
