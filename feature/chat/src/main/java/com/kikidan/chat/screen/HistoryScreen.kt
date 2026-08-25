package com.kikidan.chat.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kikidan.chat.model.HistoryState
import com.kikidan.chat.model.toUiModel
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunProgressIndicator
import com.kikidan.designsystem.component.chat.TodakunChatHistoryItem
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.chat.ConversationSummary
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
internal fun HistoryScreen(
    state: HistoryState,
    onBackClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize(),
        ) {
            TodakunSubHeader(
                title = stringResource(R.string.chat_history_title),
                onBackClick = onBackClick,
            )

            when (state) {
                is HistoryState.Loading, HistoryState.Failure -> {
                    Box(modifier = Modifier.fillMaxSize())
                }

                is HistoryState.Success -> {
                    if (state.conversations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(R.string.chat_history_empty),
                                style = TodakunTypography.body2Regular,
                                color = TodakunColor.gray500,
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.conversations.map { it.toUiModel() }, key = { it.id }) { conversation ->
                                TodakunChatHistoryItem(
                                    title = conversation.title,
                                    relativeTime = conversation.relativeTime,
                                    isUnread = conversation.isUnread,
                                    onClick = { onConversationClick(conversation.id) },
                                    onDeleteClick = { onDeleteClick(conversation.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        NewChatButton(
            text = stringResource(R.string.chat_history_new_chat),
            painter = painterResource(id = R.drawable.ic_chat_add),
            onClick = onNewChatClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 60.dp),
        )

        when (state) {
            is HistoryState.Loading -> TodakunProgressIndicator()
            is HistoryState.Success, HistoryState.Failure -> Unit
        }
    }
}

@Composable
private fun NewChatButton(
    text: String,
    painter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressedOverlay = TodakunColor.gray975.copy(alpha = 0.16f)
    val containerColor =
        if (isPressed) pressedOverlay.compositeOver(TodakunColor.primary700) else TodakunColor.primary700
    val shape = RoundedCornerShape(percent = 50)

    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        Button(
            onClick = onClick,
            modifier =
                modifier.dropShadow(
                    shape = shape,
                    shadow =
                        Shadow(
                            radius = 20.dp,
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            color = TodakunColor.black.copy(alpha = 0.06f),
                        ),
                ),
            shape = shape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = TodakunColor.white,
                ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            interactionSource = interactionSource,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = text,
                    style = TodakunTypography.body2Medium,
                )
            }
        }
    }
}

private val previewConversations =
    persistentListOf(
        ConversationSummary(
            id = "1",
            title = "오늘 나의 행운의 숫자는?",
            lastMessageAt = Instant.now().minus(30, ChronoUnit.MINUTES),
            unread = true,
        ),
        ConversationSummary(
            id = "2",
            title = "이직할까 말까?",
            lastMessageAt = Instant.now().minus(3, ChronoUnit.HOURS),
            unread = false,
        ),
        ConversationSummary(
            id = "3",
            title = "이번 달 큰 지출 해도 괜찮을까",
            lastMessageAt = Instant.now().minus(1, ChronoUnit.DAYS),
            unread = false,
        ),
    )

@Preview(showBackground = true, name = "목록")
@Composable
private fun HistoryScreenListPreview() {
    TodakunTheme {
        HistoryScreen(
            state = HistoryState.Success(previewConversations),
            onBackClick = {},
            onConversationClick = {},
            onDeleteClick = {},
            onNewChatClick = {},
        )
    }
}

@Preview(showBackground = true, name = "빈 목록")
@Composable
private fun HistoryScreenEmptyPreview() {
    TodakunTheme {
        HistoryScreen(
            state = HistoryState.Success(persistentListOf()),
            onBackClick = {},
            onConversationClick = {},
            onDeleteClick = {},
            onNewChatClick = {},
        )
    }
}
