package com.kikidan.chat.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kikidan.chat.util.toCharacterResourceId
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.chat.ChatCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatGreetingOverlay(
    category: ChatCategory,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = category.toTitle()
    val body = category.toBody()
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF000000).copy(alpha = 0.4f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent().changes.forEach(PointerInputChange::consume)
                        }
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OverlayCharacterImage(
                category = category,
                size = ChatGreetingDefaults.CharacterSize,
            )
            Spacer(Modifier.height(36.dp))
            SpeechBubble(
                modifier = Modifier.padding(horizontal = 20.dp),
                title = title,
                body = body,
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
                    .clip(ChatGreetingDefaults.CardShape)
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
                contentDescription = stringResource(R.string.chat_greeting_close_description),
                tint = TodakunColor.coolGray600,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(ChatGreetingDefaults.CloseIconSize)
                        .clickable(onClick = onCloseClick),
            )
        }
    }
}

@Composable
private fun ChatCategory.toTitle() =
    when (this) {
        ChatCategory.LOVE -> stringResource(R.string.chat_greeting_love_title)
        ChatCategory.MONEY -> stringResource(R.string.chat_greeting_money_title)
        ChatCategory.ACHIEVEMENT -> stringResource(R.string.chat_greeting_achievement_title)
        ChatCategory.HEALTH -> stringResource(R.string.chat_greeting_health_title)
        ChatCategory.RELATIONSHIP -> stringResource(R.string.chat_greeting_relationship_title)
    }

@Composable
private fun ChatCategory.toBody() =
    when (this) {
        ChatCategory.LOVE -> stringResource(R.string.chat_greeting_love_body)
        ChatCategory.MONEY -> stringResource(R.string.chat_greeting_money_body)
        ChatCategory.ACHIEVEMENT -> stringResource(R.string.chat_greeting_achievement_body)
        ChatCategory.HEALTH -> stringResource(R.string.chat_greeting_health_body)
        ChatCategory.RELATIONSHIP -> stringResource(R.string.chat_greeting_relationship_body)
    }

@Composable
private fun OverlayCharacterImage(
    category: ChatCategory,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val resourceId = category.toCharacterResourceId()
    Image(
        modifier = modifier.size(size),
        painter = painterResource(resourceId),
        contentDescription = stringResource(R.string.chat_charactor_content_description),
    )
}

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

private object ChatGreetingDefaults {
    val CharacterSize = 220.dp
    val CardShape = RoundedCornerShape(12.dp)
    val CloseIconSize = 20.dp
}

@Preview
@Composable
private fun ChatGreetingOverlayWithTitlePreview() {
    TodakunTheme {
        ChatGreetingOverlay(
            category = ChatCategory.ACHIEVEMENT,
            onCloseClick = {},
        )
    }
}
