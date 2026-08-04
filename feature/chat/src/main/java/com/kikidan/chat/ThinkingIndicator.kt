package com.kikidan.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme

// 점 3개가 순차로 흐려졌다 진해진다. designsystem에 없어 이 화면 전용으로 만든다.
@Composable
internal fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.chat_thinking_description)
    val transition = rememberInfiniteTransition(label = "thinking")
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(ThinkingIndicatorDefaults.DotSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(ThinkingIndicatorDefaults.DOT_COUNT) { index ->
            val alpha by transition.animateFloat(
                initialValue = ThinkingIndicatorDefaults.MinAlpha,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = ThinkingIndicatorDefaults.CycleMillis,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        index * ThinkingIndicatorDefaults.CycleMillis / ThinkingIndicatorDefaults.DOT_COUNT,
                    ),
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(ThinkingIndicatorDefaults.DotSize)
                    .clip(CircleShape)
                    .background(TodakunColor.gray400.copy(alpha = alpha)),
            )
        }
    }
}

private object ThinkingIndicatorDefaults {
    const val DOT_COUNT = 3
    const val MinAlpha = 0.2f
    const val CycleMillis = 600
    val DotSize = 8.dp
    val DotSpacing = 6.dp
}

@Preview(showBackground = true)
@Composable
private fun ThinkingIndicatorPreview() {
    TodakunTheme {
        ThinkingIndicator()
    }
}
