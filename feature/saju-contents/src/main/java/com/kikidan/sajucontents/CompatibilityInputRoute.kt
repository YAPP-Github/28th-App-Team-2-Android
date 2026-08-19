package com.kikidan.sajucontents

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.sajucontents.model.CompatibilityEntrySideEffect
import com.kikidan.sajucontents.screen.CompatibilityInputScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun CompatibilityEntryRoute(
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onNavigateMyPage: () -> Unit,
    onNavigateToPartnerForm: () -> Unit,
    onNavigateToResult: (compatibilityId: String, partnerLinkId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompatibilityInputViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val loadErrorMessage = stringResource(R.string.compatibility_load_error)
    val createErrorMessage = stringResource(R.string.compatibility_create_error)

    LaunchedEffect(Unit) {
        viewModel.loadMyInfo()
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            CompatibilityEntrySideEffect.ShowLoadError -> {
                snackbarHostState.showSnackbar(loadErrorMessage)
            }

            CompatibilityEntrySideEffect.ShowCreateError -> {
                snackbarHostState.showSnackbar(createErrorMessage)
            }

            CompatibilityEntrySideEffect.NavigateToPartnerForm -> {
                onNavigateToPartnerForm()
            }

            is CompatibilityEntrySideEffect.NavigateToResult -> {
                onNavigateToResult(effect.compatibilityId, effect.partnerLinkId)
            }
        }
    }

    CompatibilityInputScreen(
        state = state,
        onBackClick = onNavigateBack,
        onChangeMyInfo = onNavigateMyPage,
        onOpenPartnerPicker = viewModel::openPartnerPicker,
        onDismissPartnerPicker = viewModel::dismissPartnerPicker,
        onAddNewPartner = viewModel::addNewPartner,
        onSelectPartner = viewModel::selectPartner,
        onCheckCompatibilityClick = viewModel::checkCompatibility,
        modifier = modifier,
    )
}
