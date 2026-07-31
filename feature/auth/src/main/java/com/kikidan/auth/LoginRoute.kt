package com.kikidan.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.auth.component.toOAuthProviderType
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * `feature:auth` 모듈의 로그인 플로우 진입점.
 *
 * 스플래시 단계에서 [LoginViewModel.checkAuthState]로 로컬 토큰·서버 세션을 확인한다.
 * 이미 로그인된 상태면 로그인 화면을 거치지 않고 바로 [onAuthSuccess]로 다음 플로우(#41 온보딩/홈)에
 * 알리고, 아니면 로그인 화면으로 전환한다. 실제 OAuth 인가·서버 로그인은 [LoginViewModel]이 담당한다.
 */
@Composable
fun LoginRoute(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as Activity
    val state by viewModel.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkAuthState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            LoginSideEffect.AlreadyAuthenticated -> onAuthSuccess()

            is LoginSideEffect.LoginSucceeded -> onAuthSuccess()

            // TODO(#32): 로그인 실패 UI(스낵바/다이얼로그) 노출
            is LoginSideEffect.LoginFailed -> Unit
        }
    }

    when (state.step) {
        LoginStep.SPLASH -> {
            SplashScreen(modifier = modifier)
        }

        LoginStep.LOGIN -> {
            LoginScreen(
                onProviderClick = { provider -> viewModel.login(provider.toOAuthProviderType(), activity) },
                modifier = modifier,
            )
        }
    }
}
