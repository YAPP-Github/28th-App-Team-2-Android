package com.kikidan.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun TodakunProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackButton) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = TodakunColor.gray400,
                modifier =
                    Modifier
                        .size(width = 8.dp, height = 16.dp)
                        .clickable(onClick = onBackClick),
            )
            Spacer(modifier = Modifier.width(24.dp))
        }

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TodakunColor.gray200),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                colorStops =
                                    arrayOf(
                                        0f to TodakunColor.sky400,
                                        0.5f to TodakunColor.primary400,
                                        1f to TodakunColor.primary600,
                                    ),
                            ),
                        ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunProgressBarPreview() {
    TodakunTheme {
        TodakunProgressBar(progress = 0.5f)
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunProgressBarNoBackButtonPreview() {
    TodakunTheme {
        TodakunProgressBar(progress = 0.75f, showBackButton = false)
    }
}
