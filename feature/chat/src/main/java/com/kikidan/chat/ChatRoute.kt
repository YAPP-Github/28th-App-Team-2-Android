package com.kikidan.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.chat.model.ChatSideEffect
import com.kikidan.chat.screen.ChatScreen
import com.kikidan.chat.screen.ChatSplashScreen
import com.kikidan.designsystem.R
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val defaultErrorMessage = stringResource(R.string.chat_default_error)
    val calendarErrorMessage = stringResource(R.string.chat_calendar_error)

    LaunchedEffect(Unit) { viewModel.load(conversationId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is ChatSideEffect.ShowStreamingErrorMessage -> snackbarHostState.showSnackbar(effect.message)
            is ChatSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    if (state.isLoading && conversationId == null) {
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
            onCalendarLaunchFail = {
                scope.launch {
                    snackbarHostState.showSnackbar(calendarErrorMessage)
                }
            },
            modifier = modifier,
        )
    }
}
