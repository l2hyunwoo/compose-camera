package io.github.l2hyunwoo.compose.camera

import io.github.l2hyunwoo.compose.camera.plugin.CameraPlugin

/**
 * Immutable camera configuration.
 * Use [copy] to modify settings.
 *
 * Example:
 * ```
 * val config = CameraConfiguration()
 *     .copy(lens = CameraLens.FRONT, flashMode = FlashMode.ON)
 *     .withPlugin(qrScannerPlugin)
 * ```
 */
data class CameraConfiguration(
    val lens: CameraLens = CameraLens.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val imageFormat: ImageFormat = ImageFormat.JPEG,
    val videoQuality: VideoQuality = VideoQuality.FHD,
    val targetFps: Int = 30,
    val enableHdr: Boolean = false,
    val directory: Directory = Directory.PICTURES,
    val plugins: List<CameraPlugin> = emptyList()
) {
    /**
     * Add a plugin to the configuration
     */
    fun withPlugin(plugin: CameraPlugin): CameraConfiguration =
        copy(plugins = plugins + plugin)

    /**
     * Add multiple plugins to the configuration
     */
    fun withPlugins(vararg pluginsToAdd: CameraPlugin): CameraConfiguration =
        copy(plugins = plugins + pluginsToAdd.toList())

    /**
     * Remove a plugin from the configuration
     */
    fun withoutPlugin(pluginId: String): CameraConfiguration =
        copy(plugins = plugins.filter { it.id != pluginId })
}
