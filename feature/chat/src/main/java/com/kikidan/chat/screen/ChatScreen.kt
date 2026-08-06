package com.kikidan.chat.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.chat.component.ChatGreetingOverlay
import com.kikidan.chat.component.ThinkingIndicator
import com.kikidan.chat.model.ChatState
import com.kikidan.chat.model.StreamingChatState
import com.kikidan.chat.util.toCharacterResourceId
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.chat.TodakunChatExampleChip
import com.kikidan.designsystem.component.chat.TodakunChatHeader
import com.kikidan.designsystem.component.chat.TodakunChatInputField
import com.kikidan.designsystem.component.chat.TodakunChatUserInputBubble
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.chat.ChatCategory
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChatScreen(
    state: ChatState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onNewConversationClick: () -> Unit,
    onCloseClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var inputFieldHeight by remember { mutableStateOf(0.dp) }
    var selectedCategory: ChatCategory? by remember { mutableStateOf(null) }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null) {
            delay(ChatScreenDefaults.GREETING_DURATION_MILLIS)
            selectedCategory = null
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding()
                .imePadding(),
    ) {
        Column {
            TodakunChatHeader(
                title = stringResource(R.string.chat_header_title),
                freeChatUsed = state.quota?.remaining ?: 0,
                freeChatTotal = state.quota?.limit ?: 0,
                onCloseClick = onCloseClick,
                onChatIconClick = onNewConversationClick,
                onNotesIconClick = onHistoryClick,
            )

            ChatMessageList(
                state = state,
                modifier = Modifier.fillMaxSize(),
                suggestions = state.suggestions,
                onSuggestionClick = { suggestion ->
                    selectedCategory = suggestion.category
                    onSuggestionClick(suggestion.seedPrompt)
                },
                inputFieldHeight = inputFieldHeight,
                selectedCategory = selectedCategory,
            )
        }
        TodakunChatInputField(
            value = state.input,
            onValueChange = onInputChange,
            onSendClick = onSendClick,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .onPlaced { placeable ->
                        inputFieldHeight =
                            with(density) {
                                placeable.size.height.toDp()
                            }
                    }.padding(20.dp),
        )

        AnimatedVisibility(
            visible = selectedCategory != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            selectedCategory?.let {
                ChatGreetingOverlay(
                    category = it,
                    onCloseClick = { selectedCategory = null },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatMessageList(
    inputFieldHeight: Dp,
    selectedCategory: ChatCategory?,
    state: ChatState,
    suggestions: List<ChatSuggestion>,
    onSuggestionClick: (ChatSuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 상위에서 selectedCatgeory가 null이 되도 이전 캐릭터 유지
    var prevCategory by remember { mutableStateOf(selectedCategory) }
    if (selectedCategory != null) prevCategory = selectedCategory

    val listState = rememberLazyListState()
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

    // 키보드가 열리고 닫히는 애니메이션 프레임마다 마지막 아이템으로 재스크롤해 입력창에 가려지지 않게 한다.
    LaunchedEffect(state.messages.size, state.streamingChatState, imeBottom) {
        if (listState.layoutInfo.totalItemsCount > 0) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = inputFieldHeight,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ProfileCharacterImage(size = 60.dp, category = prevCategory)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.chat_entry_question),
                style = TodakunTypography.body1Medium,
                color = TodakunColor.coolGray900,
            )
        }

        if (state.messages.isEmpty() && state.streamingChatState is StreamingChatState.Idle) {
            item {
                suggestions.forEach { suggestion ->
                    TodakunChatExampleChip(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "${suggestion.emoji} ${suggestion.label}",
                        onClick = {
                            onSuggestionClick(suggestion)
                        },
                    )
                }
            }
        }

        items(state.messages, key = { it.id }) { message ->
            when (message.role) {
                MessageRole.USER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TodakunChatUserInputBubble(text = message.content)
                    }
                }

                else -> {
                    Text(
                        text = message.content,
                        style = TodakunTypography.body2Regular,
                        color = TodakunColor.coolGray900,
                    )
                }
            }
        }

        // THINKING / TYPING 인디케이터를 마지막 슬롯에 표시
        if (state.streamingChatState !is StreamingChatState.Idle) {
            item {
                when (state.streamingChatState) {
                    is StreamingChatState.Thinking -> {
                        ThinkingIndicator(
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    is StreamingChatState.Typing -> {
                        Text(
                            text = state.streamingChatState.streamingText,
                            style = TodakunTypography.body2Regular,
                            color = TodakunColor.black,
                        )
                    }

                    is StreamingChatState.Idle -> {
                        Unit
                    } // 도달하지 않는다
                }
            }
        }
    }
}

@Composable
private fun ProfileCharacterImage(
    category: ChatCategory?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape),
    ) {
        Image(
            painter = painterResource(category?.toCharacterResourceId() ?: R.drawable.img_todak_default_pose),
            contentDescription = stringResource(R.string.chat_charactor_content_description),
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (category != null) {
                            Modifier.graphicsLayer {
                                scaleX = 1.4f
                                scaleY = 1.4f
                            }
                        } else {
                            Modifier
                        },
                    ),
        )
    }
}

private object ChatScreenDefaults {
    const val GREETING_DURATION_MILLIS = 3_000L
}

private val previewSuggestions =
    listOf(
        ChatSuggestion(
            emoji = "📅",
            label = "중요한 일정 잡기 좋은 날인지 궁금해",
            seedPrompt = "오늘 중요한 일정 잡기 좋은 날인지 알려줘",
            category = ChatCategory.LOVE,
        ),
        ChatSuggestion(
            emoji = "💼",
            label = "커리어 운세가 궁금해",
            seedPrompt = "오늘 커리어 운세를 알려줘",
            category = ChatCategory.ACHIEVEMENT,
        ),
        ChatSuggestion(emoji = "💕", label = "오늘 연애운이 궁금해", seedPrompt = "오늘 연애운을 알려줘", category = ChatCategory.LOVE),
        ChatSuggestion(
            emoji = "💰",
            label = "재물운이 어떤지 알고 싶어",
            seedPrompt = "오늘 재물운을 알려줘",
            category = ChatCategory.MONEY,
        ),
        ChatSuggestion(
            emoji = "🏥",
            label = "건강 관리에 좋은 날인지 궁금해",
            seedPrompt = "오늘 건강운을 알려줘",
            category = ChatCategory.HEALTH,
        ),
    )

private val previewMessages =
    listOf(
        ChatMessage(
            id = "1",
            role = MessageRole.USER,
            content = "오늘 운세가 궁금해",
            status = MessageStatus.COMPLETED,
            action = null,
            createdAt = Instant.EPOCH,
        ),
        ChatMessage(
            id = "2",
            role = MessageRole.ASSISTANT,
            content = "오늘의 운세를 알아볼게요! 대체로 긍정적인 에너지가 흐르는 날입니다.",
            status = MessageStatus.COMPLETED,
            action = null,
            createdAt = Instant.EPOCH,
        ),
        ChatMessage(
            id = "3",
            role = MessageRole.USER,
            content = "더 자세히 알려줄 수 있어?",
            status = MessageStatus.COMPLETED,
            action = null,
            createdAt = Instant.EPOCH,
        ),
    )

@Preview(showBackground = true, name = "진입 상태")
@Composable
private fun ChatScreenEntryPreview() {
    TodakunTheme {
        ChatScreen(
            state = ChatState(suggestions = previewSuggestions),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "그리팅 오버레이 (개행 있음)")
@Composable
private fun ChatScreenGreetingWithTitlePreview() {
    TodakunTheme {
        ChatScreen(
            state =
                ChatState(
                    suggestions = previewSuggestions,
                    greeting = "성취운을 알려줄게!\n커리어, 학업, 목표 등 궁금한 점이나 고민은 전부 물어봐줘.",
                ),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "그리팅 오버레이 (개행 없음)")
@Composable
private fun ChatScreenGreetingNoTitlePreview() {
    TodakunTheme {
        ChatScreen(
            state =
                ChatState(
                    suggestions = previewSuggestions,
                    greeting = "오늘도 좋은 하루 되세요! 궁금한 것들을 물어봐줘.",
                ),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "생각 중 (THINKING)")
@Composable
private fun ChatScreenThinkingPreview() {
    TodakunTheme {
        ChatScreen(
            state =
                ChatState(
                    messages = previewMessages.take(1),
                ),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "타이핑 중 (TYPING)")
@Composable
private fun ChatScreenTypingPreview() {
    TodakunTheme {
        ChatScreen(
            state =
                ChatState(
                    messages = previewMessages.take(1),
                    streamingChatState = StreamingChatState.Typing("오늘의 운세를 알아볼게요! 대체로 긍정"),
                ),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}

@Preview(showBackground = true, name = "대화 이어보기")
@Composable
private fun ChatScreenConversationPreview() {
    TodakunTheme {
        ChatScreen(
            state =
                ChatState(
                    messages = previewMessages,
                    streamingChatState = StreamingChatState.Idle,
                ),
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}
