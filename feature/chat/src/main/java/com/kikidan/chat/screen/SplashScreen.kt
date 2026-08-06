package com.kikidan.chat.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun ChatSplashScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFFF2F0FB))
                .padding(vertical = 178.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.chat_splash_brand_primary),
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.gray975,
            )
            Spacer(
                modifier =
                    Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(TodakunColor.coolGray300),
            )
            Text(
                text = stringResource(R.string.chat_splash_brand_secondary),
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.gray975,
            )
        }

        Image(
            painter = painterResource(id = R.drawable.img_todak_chat_thumbnail),
            contentDescription = stringResource(R.string.chat_charactor_content_description),
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
        )

        Text(
            text = stringResource(R.string.chat_splash_tagline),
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.primary900,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatSplashScreenPreview() {
    TodakunTheme {
        ChatSplashScreen()
    }
}
