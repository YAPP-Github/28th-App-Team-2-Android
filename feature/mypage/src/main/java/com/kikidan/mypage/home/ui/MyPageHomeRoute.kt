package com.kikidan.mypage.home.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.mypage.home.MyPageHomeViewModel
import com.kikidan.mypage.home.model.MyPageHomeSideEffect
import com.kikidan.mypage.home.model.MyPageMenuType
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private const val INQUIRY_EMAIL = "todakun28@gmail.com"

@Composable
fun MyPageHomeRoute(
    onNavigateToEdit: () -> Unit,
    onNavigateToMansaeryeok: () -> Unit,
    onNavigateToPartnerSajuManagement: () -> Unit,
    onNavigateToNotificationSetting: () -> Unit,
    onNavigateToAppSetting: () -> Unit,
    onNavigateToLogin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: MyPageHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val inquiryEmailErrorMessage = stringResource(R.string.mypage_inquiry_email_error)
    val logoutErrorMessage = stringResource(R.string.mypage_logout_error)

    viewModel.collectSideEffect { effect ->
        when (effect) {
            MyPageHomeSideEffect.NavigateToLogin -> {
                onNavigateToLogin()
            }

            MyPageHomeSideEffect.ShowLogoutError -> {
                snackbarHostState.showSnackbar(logoutErrorMessage)
            }
        }
    }

    MyPageHomeScreen(
        uiState = uiState,
        modifier = modifier,
        onEditClick = onNavigateToEdit,
        onViewMansaeryeokClick = onNavigateToMansaeryeok,
        onMenuItemClick = { menuType ->
            when (menuType) {
                MyPageMenuType.SAJU_INFO -> {
                    onNavigateToPartnerSajuManagement()
                }

                MyPageMenuType.NOTIFICATION_SETTING -> {
                    onNavigateToNotificationSetting()
                }

                MyPageMenuType.APP_SETTING -> {
                    onNavigateToAppSetting()
                }

                MyPageMenuType.INQUIRY -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$INQUIRY_EMAIL"))
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        scope.launch { snackbarHostState.showSnackbar(inquiryEmailErrorMessage) }
                    }
                }

                MyPageMenuType.LOGOUT -> {
                    viewModel.logout()
                }
            }
        },
    )
}
