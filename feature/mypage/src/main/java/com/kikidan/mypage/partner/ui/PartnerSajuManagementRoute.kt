package com.kikidan.mypage.partner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.partner.PartnerSajuManagementViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun PartnerSajuManagementRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAddPartnerClick: () -> Unit = {},
    onEditPartnerClick: (String) -> Unit = {},
    viewModel: PartnerSajuManagementViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    PartnerSajuManagementScreen(
        uiState = uiState,
        modifier = modifier,
        onBackClick = onNavigateBack,
        onAddPartnerClick = onAddPartnerClick,
        onEditPartnerClick = onEditPartnerClick,
        onDeletePartnerClick = viewModel::deletePartner,
    )
}
