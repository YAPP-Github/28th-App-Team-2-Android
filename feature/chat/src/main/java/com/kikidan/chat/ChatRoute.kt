package com.kikidan.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.screen.ChatScreen
import com.kikidan.chat.screen.ChatSplashScreen
import com.kikidan.designsystem.R
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

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
    val showSplash = state.quota == null

    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowStreamingErrorMessage -> snackbarHostState.showSnackbar(effect.message)
            is ChatSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    if (showSplash) {
        ChatSplashScreen()
    } else {
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
}
