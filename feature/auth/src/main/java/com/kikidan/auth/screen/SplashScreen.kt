package com.kikidan.auth.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikidan.auth.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(SplashBackgroundColor),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_login_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_login_character),
                contentDescription = stringResource(id = R.string.login_logo_content_description),
                modifier = Modifier.size(width = 164.dp, height = 160.dp),
            )
            Spacer(modifier = Modifier.height(52.dp))
            Text(
                text = stringResource(id = R.string.login_app_name),
                // 스플래시 전용 대형 워드마크. 타이포 스케일(최대 32sp)을 넘어서는 값이라 토큰이 없다.
                style =
                    TodakunTypography.heading1ExtraBold.copy(
                        fontSize = 60.sp,
                        lineHeight = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                color = TodakunColor.white,
            )
        }
    }
}

// 스플래시 전용 다크 배경. 브랜드 토큰 스케일에 없는 화면 전용 값이다.
private val SplashBackgroundColor = Color(0xFF00010B)

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SplashScreenPreview() {
    TodakunTheme {
        SplashScreen()
    }
}
