package com.kikidan.onboarding.screen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import kotlinx.coroutines.delay

@Composable
internal fun CompleteScreen(
    isLoading: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinish by rememberUpdatedState(onFinish)
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(3000L)
            currentOnFinish()
        }
    }
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF010018)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.onboarding_complete_welcome),
                style = TodakunTypography.body2Medium,
                color = TodakunColor.whiteOpacity60,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Image(
                painter = painterResource(id = R.drawable.img_onboarding_complete_character),
                contentDescription = null,
                modifier = Modifier.size(378.dp),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.onboarding_complete_analyzing),
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.white,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompleteScreenPreview() {
    TodakunTheme {
        CompleteScreen(
            isLoading = false,
            onFinish = {},
        )
    }
}
