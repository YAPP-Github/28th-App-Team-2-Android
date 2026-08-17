package com.kikidan.luckaction.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory

@Composable
internal fun LuckActionCompleteOverlay(
    category: FortuneCategory,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.blackOpacity50)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onCloseClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.img_luck_action_complete),
                contentDescription = stringResource(R.string.luck_action_complete_character_content_description),
                modifier = Modifier.size(width = 220.dp, height = 229.dp),
            )
            Spacer(Modifier.height(36.dp))
            SpeechBubble(
                modifier = Modifier.padding(horizontal = 20.dp),
                title = stringResource(R.string.luck_action_complete_title),
                body = category.completeMessage(),
                onCloseClick = onCloseClick,
            )
        }
    }
}

@Composable
private fun SpeechBubble(
    title: String,
    body: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 20.dp, height = 12.dp)
                .background(TodakunColor.white, BubbleTail),
        )

        Box(
            modifier =
                Modifier
                    .clip(LuckActionCompleteOverlayDefaults.CardShape)
                    .background(TodakunColor.white)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = TodakunTypography.heading4Bold,
                    color = TodakunColor.coolGray900,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = body,
                    style = TodakunTypography.body2Regular,
                    color = TodakunColor.coolGray600,
                    textAlign = TextAlign.Center,
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.header_close_content_description),
                tint = TodakunColor.coolGray600,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(LuckActionCompleteOverlayDefaults.CloseIconSize)
                        .clickable(onClick = onCloseClick),
            )
        }
    }
}

@Composable
private fun FortuneCategory.completeMessage(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_complete_message_relationship
            FortuneCategory.LOVE -> R.string.luck_action_complete_message_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_complete_message_achievement
            FortuneCategory.MONEY -> R.string.luck_action_complete_message_money
            FortuneCategory.HEALTH -> R.string.luck_action_complete_message_health
        },
    )

private object BubbleTail : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Generic(
            Path().apply {
                moveTo(0f, size.height)
                lineTo(size.width, size.height)
                lineTo(size.width / 2f, 0f)
                close()
            },
        )
}

private object LuckActionCompleteOverlayDefaults {
    val CardShape = RoundedCornerShape(12.dp)
    val CloseIconSize = 20.dp
}

@Preview
@Composable
private fun LuckActionCompleteOverlayPreview() {
    TodakunTheme {
        LuckActionCompleteOverlay(
            category = FortuneCategory.LOVE,
            onCloseClick = {},
        )
    }
}
