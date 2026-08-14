package com.kikidan.home.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        modifier = modifier.size(width = 170.dp, height = 85.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val strokeWidthDp = 18.6.dp
        Canvas(modifier = Modifier.size(width = 170.dp, height = 85.dp)) {
            val stroke = Stroke(width = strokeWidthDp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = TodakunColor.gray100,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = stroke,
            )
            drawArc(
                brush = Brush.horizontalGradient(listOf(band.start, band.end)),
                startAngle = 180f,
                sweepAngle = 180f * (score.coerceIn(0, 100) / 100f),
                useCenter = false,
                style = stroke,
            )
        }
        Text(
            text = score.toString(),
            style = TodakunTypography.heading2Bold,
            color = band.end,
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
