package com.kikidan.sajucontents.dayfortune.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.sajucontents.R
import com.kikidan.designsystem.R as DesignSystemR

/** 카테고리 라벨 하나 + 별 N개(D9: 상한 없음, 응답 star 값 그대로). */
@Composable
fun CategoryStarItem(
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
                        painter = painterResource(id = DesignSystemR.drawable.ic_star_fill),
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
