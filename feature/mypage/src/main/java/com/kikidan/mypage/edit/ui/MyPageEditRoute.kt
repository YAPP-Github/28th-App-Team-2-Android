package com.kikidan.mypage.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.edit.MyPageEditViewModel
import com.kikidan.mypage.edit.model.MyPageEditSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun MyPageEditRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is MyPageEditSideEffect.NavigateBack -> onNavigateBack()
        }
    }

    MyPageEditScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onGenderSelect = viewModel::selectGender,
        onDateTypeSelect = viewModel::selectDateType,
        onBirthDateChange = viewModel::updateBirthDate,
        onBirthTimeChange = viewModel::updateBirthTime,
        onCurrentSituationChange = viewModel::updateCurrentSituation,
        onSaveClick = viewModel::save,
    )
}
