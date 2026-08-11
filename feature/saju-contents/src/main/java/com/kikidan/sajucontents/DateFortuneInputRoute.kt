package com.kikidan.sajucontents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.component.TodakunSnackbar
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.model.DateFortuneSideEffect
import com.kikidan.sajucontents.screen.DateFortuneInputScreen
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val TOAST_DURATION_MS = 2000L

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

    Box(modifier = modifier.fillMaxSize()) {
        DateFortuneInputScreen(
            state = state,
            onPurposeSelect = viewModel::onPurposeSelect,
            onOpenDateSheet = viewModel::onOpenDateSheet,
            onCloseDateSheet = viewModel::onCloseDateSheet,
            onDateToggle = viewModel::onDateToggle,
            onDateRemove = viewModel::onDateRemove,
            onReset = viewModel::onReset,
            onSubmit = viewModel::onSubmit,
        )

        val currentToastMessage = toastMessage
        if (currentToastMessage != null) {
            LaunchedEffect(currentToastMessage) {
                delay(TOAST_DURATION_MS)
                toastMessage = null
            }
            TodakunSnackbar(
                text = currentToastMessage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            )
        }
    }
}
