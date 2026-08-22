package com.kikidan.mypage.partner.form.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.partner.form.PartnerSajuFormViewModel
import com.kikidan.mypage.partner.form.model.PartnerSajuFormSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PartnerSajuFormRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    partnerLinkId: String? = null,
    viewModel: PartnerSajuFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    LaunchedEffect(partnerLinkId) { viewModel.load(partnerLinkId) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PartnerSajuFormSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    PartnerSajuFormScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onNameChange = viewModel::updateName,
        onGenderSelect = viewModel::selectGender,
        onDateTypeSelect = viewModel::selectDateType,
        onBirthDateChange = viewModel::updateBirthDate,
        onBirthTimeChange = viewModel::updateBirthTime,
        onRelationshipTypeSelect = viewModel::selectRelationshipType,
        onSaveClick = viewModel::save,
    )
}
