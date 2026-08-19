package com.kikidan.sajucontents

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.model.YearFortuneResultSideEffect
import com.kikidan.sajucontents.screen.YearFortuneResultScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.kikidan.designsystem.R as DesignSystemR

// id를 직접 전달받아 스스로 데이터를 불러온다. 공유 딥링크로 바로 진입할 수 있도록
// 연도 선택 화면과 별개인 YearFortuneResultViewModel을 사용한다.
@Composable
fun YearFortuneResultRoute(
    id: String,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YearFortuneResultViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val loadErrorMessage = stringResource(R.string.year_fortune_load_error)
    val shareErrorMessage = stringResource(DesignSystemR.string.fortune_share_kakao_error)
    val urlCopiedMessage = stringResource(DesignSystemR.string.fortune_share_url_copied)

    LaunchedEffect(id) {
        viewModel.load(id)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            YearFortuneResultSideEffect.ShowError -> snackbarHostState.showSnackbar(loadErrorMessage)
            YearFortuneResultSideEffect.ShowShareError -> snackbarHostState.showSnackbar(shareErrorMessage)
            YearFortuneResultSideEffect.ShowUrlCopied -> snackbarHostState.showSnackbar(urlCopiedMessage)
        }
    }

    YearFortuneResultScreen(
        state = state,
        onBackClick = onNavigateBack,
        onShareClick = viewModel::showShareDialog,
        onShareDismiss = viewModel::hideShareDialog,
        onKakaoShareFail = viewModel::notifyShareUnavailable,
        onUrlCopy = viewModel::notifyUrlCopied,
        onAskTodakClick = onAskTodakClick,
        modifier = modifier,
    )
}
