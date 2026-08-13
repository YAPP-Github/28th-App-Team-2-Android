package com.kikidan.sajucontents.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import kotlinx.collections.immutable.ImmutableList

private object FortuneScoreCardDefaults {
    val GaugeSize = 180.dp
    val GaugeStrokeWidth = 13.dp
}

private const val GAUGE_MAX_SCORE = 100
private const val MAX_STAR = 3

@Composable
internal fun FortuneScoreCard(
    score: Int,
    title: String,
    categoryStars: ImmutableList<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
    scoreLabel: String = stringResource(id = R.string.fortune_score_label),
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.white.copy(alpha = 0.1f))
                .padding(horizontal = 20.dp, vertical = 23.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScoreGauge(score = score, scoreLabel = scoreLabel)
        Text(
            text = title,
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Start,
            modifier = Modifier.align(Alignment.Start),
        )
        FortuneCategoryStars(categoryStars = categoryStars)
    }
}

@Composable
private fun ScoreGauge(
    score: Int,
    scoreLabel: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(FortuneScoreCardDefaults.GaugeSize), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = FortuneScoreCardDefaults.GaugeStrokeWidth.toPx()
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
                style =
                    TodakunTypography.heading1Bold.copy(
                        fontSize = 40.sp,
                        lineHeight = 52.sp,
                    ),
                color = TodakunColor.white,
            )
            Text(
                text = scoreLabel,
                style = TodakunTypography.body3Regular,
                color = TodakunColor.whiteOpacity60,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FortuneCategoryStars(
    categoryStars: ImmutableList<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categoryStars.forEach { categoryStar ->
            CategoryStarItem(
                categoryStar = categoryStar,
            )
        }
    }
}

@Composable
private fun CategoryStarItem(
    categoryStar: FortuneCategoryStar,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = categoryStar.category.label(),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.white.copy(alpha = 0.6f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(MAX_STAR) { index ->
                Icon(
                    painter =
                        painterResource(
                            id = if (index < categoryStar.star) R.drawable.ic_star_filled else R.drawable.ic_star_empty,
                        ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(width = 14.dp, height = 13.dp),
                )
            }
        }
    }
}

@Composable
private fun FortuneCategory.label(): String =
    stringResource(
        id =
            when (this) {
                FortuneCategory.RELATIONSHIP -> R.string.fortune_category_relationship
                FortuneCategory.LOVE -> R.string.fortune_category_love
                FortuneCategory.ACHIEVEMENT -> R.string.fortune_category_achievement
                FortuneCategory.MONEY -> R.string.fortune_category_money
                FortuneCategory.HEALTH -> R.string.fortune_category_health
            },
    )
