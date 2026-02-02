package com.myApp27.vocabecho.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A modifier that applies a subtle press scale animation.
 * When pressed, the element scales down to 0.96; otherwise 1.0.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "pressScale"
    )
    this.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
}

/**
 * Provides animated border color for text fields based on focus state.
 * @param isFocused whether the text field is focused
 * @param focusedColor color when focused
 * @param unfocusedColor color when not focused
 * @return animated color
 */
@Composable
fun animatedBorderColor(
    isFocused: Boolean,
    focusedColor: Color = Color(0xFF3B87D9),
    unfocusedColor: Color = Color(0xFFCCCCCC)
): Color {
    val color by animateColorAsState(
        targetValue = if (isFocused) focusedColor else unfocusedColor,
        animationSpec = tween(durationMillis = 150),
        label = "borderColor"
    )
    return color
}

/**
 * Creates a remembered interaction source and returns both it and the focused state.
 */
@Composable
fun rememberFocusInteraction(): Pair<MutableInteractionSource, Boolean> {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    return interactionSource to isFocused
}
