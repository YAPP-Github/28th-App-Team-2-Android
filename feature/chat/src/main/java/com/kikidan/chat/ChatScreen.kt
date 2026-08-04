package com.kikidan.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.component.TodakunSnackbar
import com.kikidan.designsystem.component.bottomnavigation.TodakunBottomNavigation
import com.kikidan.designsystem.component.bottomnavigation.TodakunNavItem
import com.kikidan.designsystem.component.chat.TodakunChatExampleChip
import com.kikidan.designsystem.component.chat.TodakunChatHeader
import com.kikidan.designsystem.component.chat.TodakunChatInputField
import com.kikidan.designsystem.component.chat.TodakunChatUserInputBubble
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatQuota
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant

@Composable
fun ChatScreen(
    conversationId: String?,
    onCloseClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // 컴포지션당 정확히 1회 (설계 2-3)
    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    ChatScreen(
        state = state,
        // 신규 대화 진입일 때만 그리팅이 성립한다. state.conversationId는 첫 전송 후 값이 바뀌므로
        // 진입 인자를 쓴다 (설계 2-5).
        isNewConversation = conversationId == null,
        snackbarHostState = snackbarHostState,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onSuggestionClick = viewModel::onSuggestionClick,
        onNewConversationClick = viewModel::startNewConversation,
        onCloseClick = onCloseClick,
        onHistoryClick = onNavigateToHistory,
        modifier = modifier,
    )
}

// 상태 없는 오버로드. @Preview와 (도입 시) UI 테스트가 이쪽을 쓴다.
@Composable
internal fun ChatScreen(
    state: ChatState,
    isNewConversation: Boolean,
    snackbarHostState: SnackbarHostState,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onNewConversationClick: () -> Unit,
    onCloseClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var greetingDismissed by rememberSaveable { mutableStateOf(false) }
    val showGreeting = isNewConversation && state.greeting.isNotBlank() && !greetingDismissed

    // 3초 뒤 자동 종료. X 탭도 같은 플래그를 세우므로 타이머를 따로 취소할 필요가 없다 (설계 2-5).
    LaunchedEffect(showGreeting) {
        if (showGreeting) {
            delay(ChatScreenDefaults.GREETING_DURATION_MILLIS)
            greetingDismissed = true
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
        ) {
            TodakunChatHeader(
                title = stringResource(R.string.chat_header_title),
                freeChatUsed = state.quota?.used ?: 0,
                freeChatTotal = state.quota?.limit ?: 0,
                onCloseClick = onCloseClick,
                onChatIconClick = onNewConversationClick,
                onNotesIconClick = onHistoryClick,
            )

            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty() && state.phase == ChatPhase.IDLE) {
                    ChatEntryContent(
                        suggestions = state.suggestions,
                        onSuggestionClick = onSuggestionClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    ChatMessageList(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            TodakunChatInputField(
                value = state.input,
                onValueChange = onInputChange,
                onSendClick = onSendClick,
                modifier = Modifier.padding(horizontal = ChatScreenDefaults.ContentHorizontalPadding),
            )

            // 바텀 네비게이션. 나머지 탭 화면이 없어 onItemSelect는 no-op (설계 2-6).
            TodakunBottomNavigation(
                selectedItem = TodakunNavItem.TODAK_CHAT,
                onItemSelect = {},
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding(),
        ) { data ->
            TodakunSnackbar(text = data.visuals.message)
        }

        AnimatedVisibility(
            visible = showGreeting,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ChatGreetingOverlay(
                greeting = state.greeting,
                onCloseClick = { greetingDismissed = true },
            )
        }
    }
}

@Composable
private fun ChatEntryContent(
    suggestions: List<ChatSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ChatScreenDefaults.ContentHorizontalPadding)
                .padding(top = ChatScreenDefaults.EntryTopPadding, bottom = ChatScreenDefaults.EntryBottomPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ChatScreenDefaults.EntryItemSpacing),
    ) {
        // 캐릭터 이미지 asset 미확보. 크기·위치만 잡아 두고 에셋 반입 시 Image로 교체한다 (설계 2-7).
        CharacterAvatar(size = ChatScreenDefaults.EntryAvatarSize)

        Text(
            text = stringResource(R.string.chat_entry_question),
            style = TodakunTypography.body1Medium,
            color = TodakunColor.coolGray900,
        )

        Spacer(Modifier.height(ChatScreenDefaults.EntryChipTopSpacing))

        suggestions.forEach { suggestion ->
            TodakunChatExampleChip(
                text = "${suggestion.emoji} ${suggestion.label}",
                onClick = { onSuggestionClick(suggestion.seedPrompt) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ChatMessageList(
    state: ChatState,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    // 새 메시지 또는 스트리밍 텍스트 변화 시 마지막 항목으로 스크롤한다.
    // streamingText가 16ms 틱마다 바뀌므로 animateScrollToItem을 쓰면 애니메이션이
    // 매 틱 재시작돼 덜컹거린다. scrollToItem(애니메이션 없음)으로 잔상 없이 따라간다.
    LaunchedEffect(state.messages.size, state.streamingText) {
        if (listState.layoutInfo.totalItemsCount > 0) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding =
            PaddingValues(
                horizontal = ChatScreenDefaults.ContentHorizontalPadding,
                vertical = ChatScreenDefaults.MessageListVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(ChatScreenDefaults.MessageItemSpacing),
    ) {
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

        // THINKING / TYPING 인디케이터를 마지막 슬롯에 표시한다 (설계 2-4).
        if (state.phase != ChatPhase.IDLE) {
            item {
                when (state.phase) {
                    ChatPhase.THINKING -> {
                        ThinkingIndicator(
                            modifier = Modifier.padding(vertical = ChatScreenDefaults.IndicatorVerticalPadding),
                        )
                    }

                    ChatPhase.TYPING -> {
                        Text(
                            text = state.streamingText,
                            style = TodakunTypography.body2Regular,
                            color = TodakunColor.coolGray900,
                        )
                    }

                    ChatPhase.IDLE -> {
                        Unit
                    } // 도달하지 않는다
                }
            }
        }
    }
}

// 캐릭터 이미지 asset 미확보. 크기·위치만 잡아 두고 에셋 반입 시 Image로 교체한다 (설계 2-7).
// ChatGreetingOverlay와 공유하므로 internal.
@Composable
internal fun CharacterAvatar(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(TodakunColor.primary100),
    )
}

private object ChatScreenDefaults {
    const val GREETING_DURATION_MILLIS = 3_000L // ponytail: 실기기에서 3초가 짧으면 늘린다
    val ContentHorizontalPadding = 16.dp
    val EntryTopPadding = 40.dp
    val EntryBottomPadding = 20.dp
    val EntryItemSpacing = 16.dp
    val EntryChipTopSpacing = 4.dp
    val EntryAvatarSize = 60.dp
    val MessageListVerticalPadding = 12.dp
    val MessageItemSpacing = 12.dp
    val IndicatorVerticalPadding = 8.dp
}

// ──────────────── Preview ────────────────

private val previewSuggestions =
    listOf(
        ChatSuggestion(
            emoji = "📅",
            label = "중요한 일정 잡기 좋은 날인지 궁금해",
            seedPrompt = "오늘 중요한 일정 잡기 좋은 날인지 알려줘",
            category = "schedule",
        ),
        ChatSuggestion(emoji = "💼", label = "커리어 운세가 궁금해", seedPrompt = "오늘 커리어 운세를 알려줘", category = "career"),
        ChatSuggestion(emoji = "💕", label = "오늘 연애운이 궁금해", seedPrompt = "오늘 연애운을 알려줘", category = "love"),
        ChatSuggestion(emoji = "💰", label = "재물운이 어떤지 알고 싶어", seedPrompt = "오늘 재물운을 알려줘", category = "money"),
        ChatSuggestion(emoji = "🏥", label = "건강 관리에 좋은 날인지 궁금해", seedPrompt = "오늘 건강운을 알려줘", category = "health"),
        ChatSuggestion(emoji = "🎓", label = "공부하기 좋은 날인지 알고 싶어", seedPrompt = "오늘 학업운을 알려줘", category = "study"),
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
            isNewConversation = true,
            snackbarHostState = remember { SnackbarHostState() },
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
            isNewConversation = true,
            snackbarHostState = remember { SnackbarHostState() },
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
            isNewConversation = true,
            snackbarHostState = remember { SnackbarHostState() },
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
                    phase = ChatPhase.THINKING,
                ),
            isNewConversation = false,
            snackbarHostState = remember { SnackbarHostState() },
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
                    phase = ChatPhase.TYPING,
                    streamingText = "오늘의 운세를 알아볼게요! 대체로 긍정",
                ),
            isNewConversation = false,
            snackbarHostState = remember { SnackbarHostState() },
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
                    phase = ChatPhase.IDLE,
                    quota = ChatQuota(used = 1, limit = 3),
                ),
            isNewConversation = false,
            snackbarHostState = remember { SnackbarHostState() },
            onInputChange = {},
            onSendClick = {},
            onSuggestionClick = {},
            onNewConversationClick = {},
            onCloseClick = {},
            onHistoryClick = {},
        )
    }
}
