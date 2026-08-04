package com.kikidan.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.designsystem.R as DesignR

/**
 * 최초 진입 시 3초간 떴다가 페이드아웃되는 그리팅 말풍선 (Figma 3112:29138).
 * 표시 여부와 3초 타이머는 호출부(ChatScreen)가 관리한다 — 이 Composable은 그리기만 한다.
 */
@Composable
internal fun ChatGreetingOverlay(
    greeting: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 개행이 있으면 첫 줄이 제목, 없으면 제목 없이 전체를 부제로 그린다.
    // greeting 스키마가 확정되기 전까지 어느 쪽이 와도 문구가 유실되지 않는다 (설계 2-5).
    val parts = greeting.split("\n", limit = 2)
    val title = parts.getOrNull(1)?.let { parts[0].trim() }
    val body = parts.getOrNull(1)?.trim() ?: greeting

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.blackOpacity30) // Figma 40%, 근접 토큰 (설계 2-5)
                // 오버레이 아래 입력창/칩이 눌리지 않도록 터치를 흡수한다.
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 말풍선 카드
            Box(
                modifier =
                    Modifier
                        .widthIn(max = ChatGreetingDefaults.CardMaxWidth)
                        .clip(ChatGreetingDefaults.CardShape)
                        .background(TodakunColor.white)
                        .padding(ChatGreetingDefaults.CardPadding),
            ) {
                Column(modifier = Modifier.padding(end = ChatGreetingDefaults.CloseIconSize + 4.dp)) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = TodakunTypography.heading4Bold,
                            color = TodakunColor.coolGray900,
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        text = body,
                        style = TodakunTypography.body2Regular,
                        color = TodakunColor.coolGray600,
                    )
                }
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_close),
                    contentDescription = stringResource(R.string.chat_greeting_close_description),
                    tint = TodakunColor.coolGray600,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(ChatGreetingDefaults.CloseIconSize)
                            .clickable(onClick = onCloseClick),
                )
            }
            // 말풍선 꼬리. Shape 클래스를 새로 만들지 않고 GenericShape 한 개로 끝낸다.
            Box(
                Modifier
                    .size(ChatGreetingDefaults.TailWidth, ChatGreetingDefaults.TailHeight)
                    .background(TodakunColor.white, BubbleTail),
            )
            Spacer(Modifier.height(ChatGreetingDefaults.CardCharacterSpacing))
            // 캐릭터 아바타 (플레이스홀더 — 설계 2-7)
            CharacterAvatar(size = ChatGreetingDefaults.CharacterSize)
        }
    }
}

// 말풍선 꼬리 삼각형. Shape 클래스를 새로 만들지 않고 Outline.Generic 한 개로 끝낸다.
private object BubbleTail : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Generic(
            Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            },
        )
}

private object ChatGreetingDefaults {
    val CharacterSize = 120.dp
    val CardMaxWidth = 280.dp
    val CardShape = RoundedCornerShape(16.dp)
    val CardPadding = 20.dp
    val CloseIconSize = 20.dp
    val TailWidth = 20.dp
    val TailHeight = 12.dp
    val CardCharacterSpacing = 8.dp
}

@Preview
@Composable
private fun ChatGreetingOverlayWithTitlePreview() {
    TodakunTheme {
        ChatGreetingOverlay(
            greeting = "성취운을 알려줄게!\n커리어, 학업, 목표 등 궁금한 점이나 고민은 전부 물어봐줘.",
            onCloseClick = {},
        )
    }
}

@Preview
@Composable
private fun ChatGreetingOverlayNoTitlePreview() {
    TodakunTheme {
        ChatGreetingOverlay(
            greeting = "오늘도 좋은 하루 되세요! 궁금한 것들을 물어봐줘.",
            onCloseClick = {},
        )
    }
}
