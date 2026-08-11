package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.FortuneCategory
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FortuneScoreCard(
    score: Int,
    title: String,
    categoryStars: ImmutableList<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.white.copy(alpha = 0.1f))
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(TodakunColor.primary400),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = score.toString(),
                    style = TodakunTypography.heading1Bold,
                    color = TodakunColor.white,
                )
                Text(
                    text = stringResource(id = R.string.date_fortune_score_label),
                    style = TodakunTypography.body3Regular,
                    color = TodakunColor.white.copy(alpha = 0.6f),
                )
            }
        }
        Text(
            text = title,
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp, bottom = if (categoryStars.isEmpty()) 0.dp else 20.dp),
        )
        FortuneCategoryStars(categoryStars = categoryStars)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FortuneCategoryStars(
    categoryStars: ImmutableList<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    if (categoryStars.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categoryStars.forEach { categoryStar ->
            CategoryStarItem(
                categoryStar = categoryStar,
                modifier = Modifier.widthIn(min = 80.dp),
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = categoryStar.category.label(),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.white.copy(alpha = 0.6f),
        )
        if (categoryStar.star > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(categoryStar.star) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star_fill),
                        contentDescription = null,
                        tint = TodakunColor.primary400,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneCategory.label(): String =
    stringResource(
        id =
            when (this) {
                FortuneCategory.RELATIONSHIP -> R.string.date_fortune_category_relationship
                FortuneCategory.LOVE -> R.string.date_fortune_category_love
                FortuneCategory.ACHIEVEMENT -> R.string.date_fortune_category_achievement
                FortuneCategory.MONEY -> R.string.date_fortune_category_money
                FortuneCategory.HEALTH -> R.string.date_fortune_category_health
            },
    )
