package com.kikidan.sajucontents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.component.TodakunSnackbar
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.screen.YearSelectionScreen
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val ERROR_SNACKBAR_DURATION_MS = 2000L

@Composable
fun YearSelectionRoute(
    onNavigateBack: () -> Unit,
    onNavigateToResult: (year: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: YearFortuneViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is YearFortuneSideEffect.NavigateToResult -> onNavigateToResult(effect.year)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        YearSelectionScreen(
            state = state,
            onBackClick = onNavigateBack,
            onYearSelect = viewModel::onYearSelect,
            onSubmit = viewModel::onSubmit,
        )

        val error = state.error
        if (error != null) {
            LaunchedEffect(error) {
                delay(ERROR_SNACKBAR_DURATION_MS)
                viewModel.onErrorDismiss()
            }
            TodakunSnackbar(
                text = error,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            )
        }
    }
}
