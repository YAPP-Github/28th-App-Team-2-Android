package com.kikidan.sajucontents.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.sajucontents.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

private object CompatibilityScoreCardDefaults {
    val GaugeSize = 180.dp
    val GaugeStrokeWidth = 13.dp
}

private const val GAUGE_MAX_SCORE = 100

@Composable
internal fun CompatibilityScoreCard(
    score: Int,
    subheadline: String,
    summary: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .hazeEffect(state = hazeState) { blurRadius = 20.dp }
                .background(TodakunColor.whiteOpacity10)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScoreGauge(score = score)
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = subheadline,
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = summary,
            style = TodakunTypography.body3Regular,
            color = TodakunColor.whiteOpacity60,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(CompatibilityScoreCardDefaults.GaugeSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = CompatibilityScoreCardDefaults.GaugeStrokeWidth.toPx()
            val arcSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

            drawArc(
                color = TodakunColor.blackOpacity10,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = TodakunColor.primary400,
                startAngle = -90f,
                sweepAngle = 360f * score.coerceIn(0, GAUGE_MAX_SCORE) / GAUGE_MAX_SCORE,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = TodakunTypography.heading1Bold.copy(fontSize = 40.sp, lineHeight = 52.sp),
                color = TodakunColor.white,
            )
            Text(
                text = stringResource(id = R.string.compatibility_score_label),
                style = TodakunTypography.body3Regular,
                color = TodakunColor.whiteOpacity60,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF00010B)
@Composable
private fun CompatibilityScoreCardPreview() {
    TodakunTheme {
        CompatibilityScoreCard(
            score = 85,
            subheadline = "함께 있을 때, 편안함이 커지는 사이예요.",
            summary = "두 분은 서로의 부족한 기운을 보완하며 평온한 안식처가 되어주는 최상의 흐름을 가지고 있습니다.",
            hazeState = HazeState(),
        )
    }
}
