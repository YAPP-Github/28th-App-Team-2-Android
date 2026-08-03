package com.kikidan.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikidan.auth.R
import com.kikidan.auth.component.SocialLoginButton
import com.kikidan.auth.component.SocialLoginProvider
import com.kikidan.auth.model.LoginState
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun LoginScreen(
    state: LoginState,
    onProviderClick: (SocialLoginProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(112.dp))
            Image(
                painter = painterResource(id = R.drawable.img_login_character),
                contentDescription = stringResource(id = R.string.login_logo_content_description),
                modifier = Modifier.size(width = 164.dp, height = 160.dp),
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(id = R.string.login_app_name),
                // 로그인 화면 워드마크. 40sp는 타이포 스케일에 없는 화면 전용 값이다.
                style =
                    TodakunTypography.heading1ExtraBold.copy(
                        fontSize = 40.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = TodakunColor.primary700,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.login_tagline),
                style = TodakunTypography.body1Medium,
                color = TodakunColor.coolGray800,
            )
        }

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SocialLoginProvider.entries.forEach { provider ->
                SocialLoginButton(
                    enabled = state !is LoginState.Loading,
                    provider = provider,
                    onClick = { onProviderClick(provider) },
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginScreenPreview() {
    TodakunTheme {
        LoginScreen(onProviderClick = {}, state = LoginState.Loading)
    }
}
