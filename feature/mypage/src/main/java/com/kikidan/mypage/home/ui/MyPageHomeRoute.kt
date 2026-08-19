package com.kikidan.mypage.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.mypage.home.MyPageHomeViewModel
import com.kikidan.mypage.home.model.MyPageMenuType
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun MyPageHomeRoute(
    onNavigateToEdit: () -> Unit,
    onNavigateToMansaeryeok: () -> Unit,
    onNavigateToPartnerSajuManagement: () -> Unit,
    onNavigateToNotificationSetting: () -> Unit,
    onNavigateToAppSetting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyPageHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()

    MyPageHomeScreen(
        uiState = uiState,
        modifier = modifier,
        onEditClick = onNavigateToEdit,
        onViewMansaeryeokClick = onNavigateToMansaeryeok,
        onMenuItemClick = { menuType ->
            when (menuType) {
                MyPageMenuType.SAJU_INFO -> onNavigateToPartnerSajuManagement()

                MyPageMenuType.NOTIFICATION_SETTING -> onNavigateToNotificationSetting()

                MyPageMenuType.APP_SETTING -> onNavigateToAppSetting()

                // TODO(#후속이슈): 문의하기 연결
                MyPageMenuType.INQUIRY -> Unit

                // TODO(#후속이슈): 로그아웃 UseCase 연동
                MyPageMenuType.LOGOUT -> Unit
            }
        },
    )
}
