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
package io.github.l2hyunwoo.compose.camera.ui

import androidx.compose.ui.geometry.Offset
import io.github.l2hyunwoo.compose.camera.core.FocusPoint

/**
 * Convert Compose [Offset] to platform-agnostic [FocusPoint].
 *
 * The values are clamped to the valid range [0, 1].
 *
 * Example:
 * ```kotlin
 * val tapOffset = Offset(0.5f, 0.5f)
 * controller.cameraControl.focus(tapOffset.toFocusPoint())
 * ```
 */
fun Offset.toFocusPoint(): FocusPoint = FocusPoint.clamped(x, y)

/**
 * Convert platform-agnostic [FocusPoint] to Compose [Offset].
 *
 * Example:
 * ```kotlin
 * val focusPoint = FocusPoint.CENTER
 * val offset = focusPoint.toOffset()
 * ```
 */
fun FocusPoint.toOffset(): Offset = Offset(x, y)
