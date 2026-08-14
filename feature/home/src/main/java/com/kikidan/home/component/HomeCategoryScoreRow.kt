package com.kikidan.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.home.model.CategoryScoreUiModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun HomeCategoryScoreRow(
    categories: PersistentList<CategoryScoreUiModel>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_section_detail_fortune),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.luckActionId }) { item ->
                CategoryScoreCard(item = item, onClick = { onCategoryClick(item.luckActionId) })
            }
        }
    }
}

@Composable
private fun CategoryScoreCard(
    item: CategoryScoreUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val band = FortuneScoreBand.of(item.score)
    Column(
        modifier =
            modifier
                .width(93.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.gray50)
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = categoryLabel(item.category),
            style = TodakunTypography.caption1Medium,
            color = TodakunColor.gray700,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "${item.score}",
            style = TodakunTypography.body1Bold,
            color = band.end,
            textAlign = TextAlign.Center,
        )
        Spacer(
            modifier =
                Modifier
                    .height(3.dp)
                    .width(24.dp)
                    .background(
                        Brush.horizontalGradient(listOf(band.start, band.end)),
                        RoundedCornerShape(99.dp),
                    ),
        )
    }
}

@Composable
private fun categoryLabel(category: FortuneCategory): String =
    stringResource(
        when (category) {
            FortuneCategory.RELATIONSHIP -> R.string.home_category_score_relationship
            FortuneCategory.LOVE -> R.string.home_category_score_love
            FortuneCategory.ACHIEVEMENT -> R.string.home_category_score_achievement
            FortuneCategory.HEALTH -> R.string.home_category_score_health
            FortuneCategory.MONEY -> R.string.home_category_score_money
        },
    )

@Preview(showBackground = true)
@Composable
private fun HomeCategoryScoreRowPreview() {
    TodakunTheme {
        HomeCategoryScoreRow(
            categories =
                persistentListOf(
                    CategoryScoreUiModel("la-1", FortuneCategory.RELATIONSHIP, 45),
                    CategoryScoreUiModel("la-2", FortuneCategory.LOVE, 84),
                    CategoryScoreUiModel("la-3", FortuneCategory.ACHIEVEMENT, 38),
                    CategoryScoreUiModel("la-4", FortuneCategory.HEALTH, 21),
                    CategoryScoreUiModel("la-5", FortuneCategory.MONEY, 72),
                ),
            onCategoryClick = {},
        )
    }
}
