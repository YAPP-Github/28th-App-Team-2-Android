package com.kikidan.sajucontents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.screen.DateFortuneResultScreen
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun DateFortuneResultRoute(
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DateFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    DateFortuneResultScreen(
        state = state,
        onBackClick = onNavigateBack,
        onTabSelect = viewModel::onResultTabSelect,
        // Swagger에 공유/캘린더 내보내기 엔드포인트가 없어 콜백만 노출한다 (설계 문서 Q7, 3-3절).
        onShareClick = {},
        onExportClick = {},
        onAskTodakClick = onAskTodakClick,
        modifier = modifier,
    )
}
