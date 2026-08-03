package com.kikidan.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.auth.component.toOAuthProviderType
import com.kikidan.auth.model.LoginSideEffect
import com.kikidan.auth.model.SplashSideEffect
import com.kikidan.auth.screen.LoginScreen
import com.kikidan.auth.screen.SplashScreen
import com.kikidan.domain.model.auth.LoginResult
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginRoute(
    onAuthSuccess: (LoginResult) -> Unit,
    onAuthPass: () -> Unit,
    modifier: Modifier = Modifier,
    splashViewModel: SplashViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current as Activity
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { splashViewModel.checkAuthState() }

    splashViewModel.collectSideEffect { effect ->
        when (effect) {
            SplashSideEffect.AlreadyAuthenticated -> onAuthPass()
            SplashSideEffect.NavigateToLogin -> showLogin = true
        }
    }

    loginViewModel.collectSideEffect { effect ->
        when (effect) {
            is LoginSideEffect.LoginSucceeded -> onAuthSuccess(effect.result)

            // TODO(#32): 로그인 실패 UI(스낵바/다이얼로그) 노출
            is LoginSideEffect.LoginFailed -> Unit
        }
    }

    if (showLogin) {
        LoginScreen(
            onProviderClick = { provider ->
                loginViewModel.login(
                    provider.toOAuthProviderType(),
                    activity,
                )
            },
            modifier = modifier,
        )
    } else {
        SplashScreen(modifier = modifier)
    }
}
