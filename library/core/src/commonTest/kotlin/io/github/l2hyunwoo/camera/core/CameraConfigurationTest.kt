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
package io.github.l2hyunwoo.camera.core

import io.github.l2hyunwoo.camera.core.plugin.CameraPlugin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraConfigurationTest {
  @Test
  fun testDefaultConfiguration() {
    val config = CameraConfiguration()

    assertEquals(CameraLens.BACK, config.lens)
    assertEquals(FlashMode.OFF, config.flashMode)
    assertEquals(ImageFormat.JPEG, config.imageFormat)
    assertEquals(VideoQuality.FHD, config.videoQuality)
    assertEquals(30, config.targetFps)
    assertEquals(false, config.enableHdr)
    assertTrue(config.plugins.isEmpty())
  }

  @Test
  fun testCopyConfiguration() {
    val original = CameraConfiguration()
    val modified = original.copy(
      lens = CameraLens.FRONT,
      flashMode = FlashMode.ON,
    )

    // Original should be unchanged
    assertEquals(CameraLens.BACK, original.lens)
    assertEquals(FlashMode.OFF, original.flashMode)

    // Modified should have new values
    assertEquals(CameraLens.FRONT, modified.lens)
    assertEquals(FlashMode.ON, modified.flashMode)

    // Other values should remain the same
    assertEquals(original.imageFormat, modified.imageFormat)
    assertEquals(original.videoQuality, modified.videoQuality)
  }

  @Test
  fun testWithPlugin() {
    val config = CameraConfiguration()
    val plugin = object : CameraPlugin {
      override val id = "test-plugin"
      override fun onAttach(controller: CameraController) {}
      override fun onDetach() {}
    }

    val withPlugin = config.withPlugin(plugin)

    assertTrue(config.plugins.isEmpty())
    assertEquals(1, withPlugin.plugins.size)
    assertEquals("test-plugin", withPlugin.plugins.first().id)
  }

  @Test
  fun testWithoutPlugin() {
    val plugin1 = object : CameraPlugin {
      override val id = "plugin-1"
      override fun onAttach(controller: CameraController) {}
      override fun onDetach() {}
    }
    val plugin2 = object : CameraPlugin {
      override val id = "plugin-2"
      override fun onAttach(controller: CameraController) {}
      override fun onDetach() {}
    }

    val config = CameraConfiguration()
      .withPlugin(plugin1)
      .withPlugin(plugin2)

    assertEquals(2, config.plugins.size)

    val withoutOne = config.withoutPlugin("plugin-1")
    assertEquals(1, withoutOne.plugins.size)
    assertEquals("plugin-2", withoutOne.plugins.first().id)
  }
}

