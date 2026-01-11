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
package io.github.l2hyunwoo.compose.camera.sample.extensions

import io.github.l2hyunwoo.compose.camera.core.CameraControlExtension
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.FocusPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extension that counts tap-to-focus events.
 *
 * This demonstrates how to create an extension that:
 * - Tracks user interactions
 * - Stores a history of focus points
 * - Exposes analytics data
 */
class TapCounterExtension : CameraControlExtension {
    override val id: String = "tap-counter"

    private var controller: CameraController? = null

    private val _tapCount = MutableStateFlow(0)
    val tapCount: StateFlow<Int> = _tapCount.asStateFlow()

    private val _focusHistory = MutableStateFlow<List<FocusPoint>>(emptyList())
    val focusHistory: StateFlow<List<FocusPoint>> = _focusHistory.asStateFlow()

    override fun onAttach(controller: CameraController) {
        this.controller = controller
    }

    override fun onDetach() {
        controller = null
    }

    /**
     * Record a tap-to-focus event.
     * Call this from your UI when the user taps to focus.
     */
    fun recordTap(focusPoint: FocusPoint) {
        _tapCount.value++
        _focusHistory.value = _focusHistory.value + focusPoint
    }

    /**
     * Reset the tap counter and history.
     */
    fun reset() {
        _tapCount.value = 0
        _focusHistory.value = emptyList()
    }

    /**
     * Get the most recent focus point, if any.
     */
    fun getLastFocusPoint(): FocusPoint? = _focusHistory.value.lastOrNull()
}
