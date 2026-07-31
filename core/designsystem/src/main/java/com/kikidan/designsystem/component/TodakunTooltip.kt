package com.kikidan.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodakunTooltip(
    text: String,
    modifier: Modifier = Modifier,
    state: TooltipState = rememberTooltipState(),
    anchor: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val maxWidth =
        with(density) {
            LocalWindowInfo.current.containerSize.width
                .toDp() - 40.dp
        }

    val positionProvider =
        remember(density) {
            with(density) {
                TodakunTooltipPositionProvider(
                    margin = 20.dp.roundToPx(),
                    spacing = 4.dp.roundToPx(),
                )
            }
        }

    TooltipBox(
        positionProvider = positionProvider,
        tooltip = {
            PlainTooltip(
                caretShape = TooltipDefaults.caretShape(TodakunTooltipDefaults.CaretSize),
                maxWidth = maxWidth,
                shape = RoundedCornerShape(99.dp),
                containerColor = TodakunColor.blackOpacity80,
                contentColor = TodakunColor.white,
            ) {
                Text(
                    text = text,
                    style = TodakunTypography.body3Medium,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier.padding(
                            horizontal = TodakunTooltipDefaults.ExtraHorizontalPadding,
                            vertical = TodakunTooltipDefaults.ExtraVerticalPadding,
                        ),
                )
            }
        },
        state = state,
        modifier = modifier,
        content = anchor,
    )
}

private object TodakunTooltipDefaults {
    val CaretSize = DpSize(width = 8.dp, height = 8.dp)

    /**
     * Figma 기준 툴팁 콘텐츠 패딩은 가로 16dp / 세로 6dp 다.
     *
     * [PlainTooltip] 은 콘텐츠에 가로 8dp / 세로 4dp 패딩을 **내부 상수로 강제 적용**하며
     * 이를 조절할 파라미터를 제공하지 않는다. 따라서 목표값에서 그만큼을 뺀 나머지를
     * 콘텐츠에 직접 더해 최종 패딩을 맞춘다.
     *
     * ⚠️ material3 가 내부 패딩 상수를 바꾸면 이 값도 함께 조정해야 한다. (기준: material3 1.4.0)
     */
    val ExtraHorizontalPadding = 8.dp
    val ExtraVerticalPadding = 2.dp
}

private class TodakunTooltipPositionProvider(
    private val margin: Int,
    private val spacing: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val centered = anchorBounds.left + (anchorBounds.width - popupContentSize.width) / 2
        val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
        val x = centered.coerceIn(margin, maxX)

        val above = anchorBounds.top - popupContentSize.height - spacing
        val y = if (above >= 0) above else anchorBounds.bottom + spacing

        return IntOffset(x, y)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TodakunTooltipShortPreview() {
    TodakunTheme {
        TodakunTooltip(
            text = "오늘 이 사람과 어디를 갈까?",
            state = rememberTooltipState(initialIsVisible = true, isPersistent = true),
        ) {
            Text(
                text = "앵커",
                modifier = Modifier.padding(48.dp),
                style = TodakunTypography.body3Medium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TodakunTooltipLongPreview() {
    TodakunTheme {
        TodakunTooltip(
            text = "오늘 이 사람과 어디를 갈까? 이 텍스트는 매우 길어서 줄바꿈 처리됩니다 오늘 이 사람과 어디를 갈까? 이 텍스트는 매우 길어서 줄바꿈 처리됩니다",
            state = rememberTooltipState(initialIsVisible = true, isPersistent = true),
        ) {
            Text(
                text = "앵커",
                modifier = Modifier.padding(48.dp),
                style = TodakunTypography.body3Medium,
            )
        }
    }
}
