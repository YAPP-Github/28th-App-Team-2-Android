package com.kikidan.sajucontents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.screen.YearFortuneResultScreen
import org.orbitmvi.orbit.compose.collectAsState

// year를 직접 전달받아 스스로 데이터를 불러온다(연도 선택 화면과 별개 ViewModel 인스턴스여도 무방).
@Composable
fun YearFortuneResultRoute(
    year: Int,
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YearFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(year) {
        viewModel.load(year)
    }

    YearFortuneResultScreen(
        state = state,
        onBackClick = onNavigateBack,
        onShareIconClick = viewModel::onShareIconClick,
        onShareSheetDismiss = viewModel::onShareSheetDismiss,
        onAskTodakClick = onAskTodakClick,
        onRetry = { viewModel.load(year) },
        modifier = modifier,
    )
}
