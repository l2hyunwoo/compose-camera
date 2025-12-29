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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A default focus indicator that shows a square box at the tapped position.
 * It animates scale and alpha to provide visual feedback.
 *
 * @param tapPosition The position where the user tapped.
 * @param size The size of the focus indicator.
 * @param color The color of the focus indicator.
 * @param strokeWidth The stroke width of the focus indicator.
 */
@Composable
fun BoxScope.DefaultFocusIndicator(
  tapPosition: Offset,
  size: Dp = 64.dp,
  // Default yellow/gold focus color
  color: Color = Color(0xFFFFB800),
  strokeWidth: Dp = 2.dp,
) {
  val scaleAnim = remember { Animatable(1.5f) }
  val alphaAnim = remember { Animatable(1f) }

  LaunchedEffect(tapPosition) {
    // Reset animations
    scaleAnim.snapTo(1.5f)
    alphaAnim.snapTo(1f)

    // Animate scale down (focus locking in)
    launch {
      scaleAnim.animateTo(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
      )
    }

    // Delay then fade out
    delay(1000)
    alphaAnim.animateTo(
      targetValue = 0f,
      animationSpec = tween(durationMillis = 500),
    )
  }

  // Draw the indicator centered at the tap position
  // We offset the box by -size/2 to center it at the tap point
  Box(
    modifier = Modifier
      .matchParentSize(),
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      if (alphaAnim.value > 0f) {
        val side = size.toPx()
        val topLeft = Offset(
          x = tapPosition.x - side / 2f,
          y = tapPosition.y - side / 2f,
        )
        val currentSize = side * scaleAnim.value
        val currentTopLeft = Offset(
          x = tapPosition.x - currentSize / 2f,
          y = tapPosition.y - currentSize / 2f,
        )

        drawRoundRect(
          color = color,
          topLeft = currentTopLeft,
          size = androidx.compose.ui.geometry.Size(currentSize, currentSize),
          style = Stroke(width = strokeWidth.toPx()),
          cornerRadius = CornerRadius(4.dp.toPx()),
          alpha = alphaAnim.value,
        )
      }
    }
  }
}
