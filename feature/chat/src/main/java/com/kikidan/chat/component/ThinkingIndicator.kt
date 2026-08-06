package com.kikidan.chat.component

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.chat.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

// 점 3개가 좌→우 순서로 튀어 오르며 옅은 색에서 primary700로 짙어진다 (Figma node 2292:21555). designsystem에 없어 이 화면 전용으로 만든다.
@Composable
internal fun ThinkingIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.chat_thinking_description)
    val transition = rememberInfiniteTransition(label = "thinking")

    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(ThinkingIndicatorDefaults.LabelGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ThinkingIndicatorDefaults.DotGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            repeat(ThinkingIndicatorDefaults.DOT_COUNT) { index ->
                val bounceProgress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                tween(
                                    durationMillis = ThinkingIndicatorDefaults.BOUNCE_MILLIS,
                                    easing = FastOutSlowInEasing,
                                ),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset =
                                StartOffset(index * ThinkingIndicatorDefaults.STAGGER_MILLIS),
                        ),
                    label = "dot$index",
                )
                Box(
                    modifier =
                        Modifier
                            .size(ThinkingIndicatorDefaults.DotSize)
                            .graphicsLayer {
                                translationY = -ThinkingIndicatorDefaults.BounceHeight.toPx() * bounceProgress
                            }
                            .clip(CircleShape)
                            .background(
                                lerp(TodakunColor.primary300, TodakunColor.primary700, bounceProgress),
                            ),
                )
            }
        }

        Text(
            text = stringResource(R.string.chat_thinking_label),
            style = TodakunTypography.body2Regular,
            color = TodakunColor.primary700,
        )
    }
}

private object ThinkingIndicatorDefaults {
    const val DOT_COUNT = 3
    const val BOUNCE_MILLIS = 450
    const val STAGGER_MILLIS = 150
    val DotSize = 4.dp
    val DotGap = 3.dp
    val BounceHeight = 3.dp
    val LabelGap = 8.dp
}

@Preview(showBackground = true)
@Composable
private fun ThinkingIndicatorPreview() {
    TodakunTheme {
        ThinkingIndicator()
    }
}
