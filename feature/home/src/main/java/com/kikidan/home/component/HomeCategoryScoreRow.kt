package com.kikidan.home.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    Column(
        modifier =
            modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.coolGray50)
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = categoryLabel(item.category),
                style = TodakunTypography.caption1Medium,
                color = TodakunColor.gray600,
            )

            Box(
                modifier =
                    Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(TodakunColor.white),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_small_right),
                    contentDescription = null,
                    tint = TodakunColor.gray400,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${item.score}점",
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
        )
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(categoryIllustration(item.category)),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

private fun categoryIllustration(category: FortuneCategory): Int =
    when (category) {
        FortuneCategory.RELATIONSHIP -> R.drawable.img_home_category_relationship
        FortuneCategory.LOVE -> R.drawable.img_home_category_love
        FortuneCategory.ACHIEVEMENT -> R.drawable.img_home_category_achievement
        FortuneCategory.HEALTH -> R.drawable.img_home_category_health
        FortuneCategory.MONEY -> R.drawable.img_home_category_money
    }

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
