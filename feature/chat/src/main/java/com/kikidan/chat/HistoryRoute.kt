package com.kikidan.chat

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.chat.model.HistorySideEffect
import com.kikidan.chat.screen.HistoryScreen
import com.kikidan.designsystem.R
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun HistoryRoute(
    onBackClick: () -> Unit,
    onNavigateToChat: (conversationId: String) -> Unit,
    onNewChatClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.chat_default_error)

    LaunchedEffect(Unit) { viewModel.load() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is HistorySideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    HistoryScreen(
        state = state,
        onBackClick = onBackClick,
        onConversationClick = onNavigateToChat,
        onDeleteClick = viewModel::onDeleteClick,
        onNewChatClick = onNewChatClick,
        modifier = modifier,
    )
}
