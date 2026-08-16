package com.kikidan.mypage.partner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.partner.PartnerSajuManagementViewModel
import com.kikidan.mypage.partner.model.PartnerSajuManagementSideEffect
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun PartnerSajuManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAddPartnerClick: () -> Unit = {},
    onEditPartnerClick: (String) -> Unit = {},
    viewModel: PartnerSajuManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()
    var showMaxPartnerSnackbar by remember { mutableStateOf(false) }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is PartnerSajuManagementSideEffect.NavigateToAddPartner -> onAddPartnerClick()
            is PartnerSajuManagementSideEffect.ShowMaxPartnerLimitSnackbar -> showMaxPartnerSnackbar = true
        }
    }

    PartnerSajuManagementScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onAddPartnerClick = viewModel::addPartner,
        onEditPartnerClick = onEditPartnerClick,
        onDeletePartnerClick = viewModel::deletePartner,
        showMaxPartnerSnackbar = showMaxPartnerSnackbar,
        onMaxPartnerSnackbarDismiss = { showMaxPartnerSnackbar = false },
    )
}
