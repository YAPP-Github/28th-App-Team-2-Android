package com.kikidan.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.model.ChatState
import com.kikidan.chat.model.StreamingChatState
import com.kikidan.chat.screen.ChatScreen
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.domain.model.chat.ChatMessage
import com.kikidan.domain.model.chat.ChatSuggestion
import com.kikidan.domain.model.chat.MessageRole
import com.kikidan.domain.model.chat.MessageStatus
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.Instant

@Composable
fun ChatRoute(
    conversationId: String?,
    onCloseClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.chat_default_error)

    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowStreamingErrorMessage -> snackbarHostState.showSnackbar(effect.message)
            is ChatSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    ChatScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onSendClick = viewModel::onSendClick,
        onSuggestionClick = viewModel::onSuggestionClick,
        onNewConversationClick = viewModel::startNewConversation,
        onCloseClick = onCloseClick,
        onHistoryClick = onNavigateToHistory,
        modifier = modifier,
    )
}
