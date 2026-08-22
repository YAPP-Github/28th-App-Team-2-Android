package com.kikidan.mypage.mansaeryeok.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.mansaeryeok.MansaeryeokDetailViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun MansaeryeokDetailRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MansaeryeokDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    MansaeryeokDetailScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
    )
}
