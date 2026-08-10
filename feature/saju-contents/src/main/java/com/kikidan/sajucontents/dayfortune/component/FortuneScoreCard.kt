package com.kikidan.sajucontents.dayfortune.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.FortuneCategoryStar
import com.kikidan.sajucontents.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FortuneScoreCard(
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
