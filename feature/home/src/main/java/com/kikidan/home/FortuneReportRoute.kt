package com.kikidan.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.home.model.FortuneReportSideEffect
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.screen.FortuneReportScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun FortuneReportRoute(
    fortuneId: String,
    snackbarHostState: SnackbarHostState,
    onNavigateToBack: () -> Unit,
    onNavigateToLuckAction: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FortuneReportViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val defaultErrorMessage = stringResource(R.string.home_default_error)
    LaunchedEffect(fortuneId) { viewModel.load(fortuneId) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is FortuneReportSideEffect.Error -> snackbarHostState.showSnackbar(defaultErrorMessage)
        }
    }

    FortuneReportScreen(
        state = state,
        onBackClick = onNavigateToBack,
        onNavigateToLuckAction = onNavigateToLuckAction,
        onDetailDismiss = viewModel::closeDetail,
        onNavigateToChat = {
            // 바텀시트를 띄운 채로 채팅에 진입하면, 채팅에서 뒤로가기 시 백스택 복귀와 함께 바텀시트가 다시 열린다.
            viewModel.closeDetail()
            onNavigateToChat()
        },
        onScoreProgressRowClick = viewModel::openDetail,
        modifier = modifier,
    )
}
