package com.kikidan.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.kikidan.auth.component.SocialLoginButton
import com.kikidan.auth.component.SocialLoginProvider
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

/**
 * 소셜 로그인 진입 화면.
 *
 * 실제 로그인 호출은 #32(OAuth)에서 붙인다. 여기서는 제공자별 클릭 콜백만 위로 흘려보낸다.
 */
@Composable
internal fun LoginScreen(
    onProviderClick: (SocialLoginProvider) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.img_login_character),
            contentDescription = stringResource(id = R.string.login_logo_content_description),
            modifier = Modifier.size(width = 164.dp, height = 160.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.login_tagline),
            style = TodakunTypography.body1Medium,
            color = TodakunColor.coolGray800,
        )
        Spacer(modifier = Modifier.weight(1.2f))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SocialLoginProvider.entries.forEach { provider ->
                SocialLoginButton(
                    provider = provider,
                    onClick = { onProviderClick(provider) },
                )
            }
        }
        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginScreenPreview() {
    TodakunTheme {
        LoginScreen(onProviderClick = {})
    }
}
