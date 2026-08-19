package com.kikidan.sajucontents

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.model.CompatibilityResultSideEffect
import com.kikidan.sajucontents.screen.CompatibilityResultScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import com.kikidan.designsystem.R as DesignSystemR

// id를 직접 전달받아 스스로 데이터를 불러온다. 공유 딥링크로 바로 진입할 수 있도록
// 궁합 입력 화면과 별개인 CompatibilityResultViewModel을 사용한다.
@Composable
fun CompatibilityResultRoute(
    compatibilityId: String,
    partnerLinkId: String?,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompatibilityResultViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val loadErrorMessage = stringResource(R.string.compatibility_load_error)
    val shareErrorMessage = stringResource(DesignSystemR.string.fortune_share_kakao_error)
    val urlCopiedMessage = stringResource(DesignSystemR.string.fortune_share_url_copied)

    LaunchedEffect(compatibilityId, partnerLinkId) {
        viewModel.load(compatibilityId, partnerLinkId)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            CompatibilityResultSideEffect.ShowError -> snackbarHostState.showSnackbar(loadErrorMessage)
            CompatibilityResultSideEffect.ShowShareError -> snackbarHostState.showSnackbar(shareErrorMessage)
            CompatibilityResultSideEffect.ShowUrlCopied -> snackbarHostState.showSnackbar(urlCopiedMessage)
        }
    }

    CompatibilityResultScreen(
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
