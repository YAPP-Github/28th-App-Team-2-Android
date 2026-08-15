package com.kikidan.home.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
internal fun FortuneScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val band = FortuneScoreBand.of(score)
    Box(
        modifier = modifier.size(width = 200.dp, height = 114.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthDp = 16.dp
            val sw = strokeWidthDp.toPx()
            val d = 170.dp.toPx()
            val topLeft =
                Offset(
                    x = (size.width - d) / 2f,
                    y = sw / 2f,
                )
            val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = TodakunColor.gray50,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = Size(d, d),
            )
            drawArc(
                brush = Brush.horizontalGradient(listOf(band.start, band.end)),
                startAngle = 180f,
                sweepAngle = 180f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                style = stroke,
                topLeft = topLeft,
                size = Size(d, d),
            )
        }
        Text(
            text = stringResource(R.string.home_today_score_value, score),
            style = TodakunTypography.heading2ExtraBold,
            color = band.end,
            modifier = Modifier.padding(bottom = 14.dp),
        )
    }
}

@Preview
@Composable
private fun FortuneScoreGaugePreview() {
    TodakunTheme {
        FortuneScoreGauge(score = 72)
    }
}
