package com.kikidan.auth

import android.app.Activity
import android.app.LocalActivityManager
import android.content.Context
import android.content.ContextWrapper
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
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun LoginRoute(
    onAuthSuccess: (LoginResult) -> Unit,
    onAuthPass: () -> Unit,
    modifier: Modifier = Modifier,
    splashViewModel: SplashViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val activity = LocalContext.current.findActivity() ?: return
    val loginState by loginViewModel.collectAsState()
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
            state = loginState,
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
