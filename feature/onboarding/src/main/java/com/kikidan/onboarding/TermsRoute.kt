package com.kikidan.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.dialog.TodakunDialog
import com.kikidan.onboarding.model.TermsDialog
import com.kikidan.onboarding.model.TermsSideEffect
import com.kikidan.onboarding.screen.TermsScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun TermsRoute(
    onNavigateOnboarding: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            TermsSideEffect.Exit -> onExit()
        }
    }

    BackHandler {
        viewModel.clickBack()
    }

    TermsScreen(
        termsAgreement = state.termsAgreement,
        canProceed = state.canProceed,
        onTermChange = viewModel::changeTerm,
        onAllTermsChange = viewModel::changeAllTerms,
        onNextClick = onNavigateOnboarding,
        onBackClick = viewModel::clickBack,
        modifier = modifier,
    )

    when (state.dialog) {
        TermsDialog.EXIT_CONFIRM -> {
            TodakunDialog(
                title = stringResource(id = R.string.onboarding_terms_exit_title),
                description = stringResource(id = R.string.onboarding_terms_exit_description),
                confirmText = stringResource(id = R.string.onboarding_terms_exit_dismiss),
                dismissText = stringResource(id = R.string.onboarding_terms_exit_confirm),
                onConfirm = viewModel::confirmExit,
                onDismiss = viewModel::dismissDialog,
            )
        }

        null -> {
            Unit
        }
    }
}
