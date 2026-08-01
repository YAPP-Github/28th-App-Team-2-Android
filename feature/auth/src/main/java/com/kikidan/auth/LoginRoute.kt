package com.kikidan.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.auth.component.toOAuthProviderType
import com.kikidan.domain.model.auth.LoginResult
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginRoute(
    onAuthSuccess: (LoginResult) -> Unit,
    onAuthPass: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as Activity
    val state by viewModel.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkAuthState() }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            LoginSideEffect.AlreadyAuthenticated -> onAuthPass()

            is LoginSideEffect.LoginSucceeded -> onAuthSuccess(effect.result)

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
