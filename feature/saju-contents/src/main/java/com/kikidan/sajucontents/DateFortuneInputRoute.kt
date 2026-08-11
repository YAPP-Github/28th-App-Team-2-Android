package com.kikidan.sajucontents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.model.DateFortuneSideEffect
import com.kikidan.sajucontents.screen.DateFortuneInputScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun DateFortuneInputRoute(
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DateFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    var toastMessage by remember { mutableStateOf<String?>(null) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is DateFortuneSideEffect.ShowToast -> {
                // getString의 남는 포맷 인자는 무시되므로 플레이스홀더가 없는 메시지에도 안전하다.
                toastMessage = context.getString(effect.messageRes, DateFortuneDefaults.MAX_TARGET_DATES)
            }

            is DateFortuneSideEffect.ShowError -> {
                toastMessage = context.getString(effect.messageRes)
            }

            DateFortuneSideEffect.NavigateToResult -> {
                onNavigateToResult()
            }

            DateFortuneSideEffect.NavigateBack -> {
                onNavigateBack()
            }
        }
    }

    DateFortuneInputScreen(
        state = state,
        toastMessage = toastMessage,
        onToastDismiss = { toastMessage = null },
        onBackClick = onNavigateBack,
        onPurposeSelect = viewModel::onPurposeSelect,
        onGenderSelect = viewModel::onGenderSelect,
        onOpenDateSheet = viewModel::onOpenDateSheet,
        onCloseDateSheet = viewModel::onCloseDateSheet,
        onDateToggle = viewModel::onDateToggle,
        onDateRemove = viewModel::onDateRemove,
        onReset = viewModel::onReset,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}
