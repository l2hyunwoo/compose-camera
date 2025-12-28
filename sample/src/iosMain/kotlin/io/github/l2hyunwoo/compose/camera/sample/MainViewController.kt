package io.github.l2hyunwoo.compose.camera.sample

import androidx.compose.ui.window.ComposeUIViewController

/**
 * iOS entry point for the sample app.
 * Use this from your Swift code:
 * ```swift
 * let controller = MainViewControllerKt.MainViewController()
 * ```
 */
fun MainViewController() = ComposeUIViewController { 
    SampleApp() 
}
