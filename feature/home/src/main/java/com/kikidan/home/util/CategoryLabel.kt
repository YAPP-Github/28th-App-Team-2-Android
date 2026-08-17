package com.kikidan.home.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kikidan.designsystem.R
import com.kikidan.domain.model.fortune.FortuneCategory

@Composable
internal fun categoryLabel(category: FortuneCategory): String =
    stringResource(
        when (category) {
            FortuneCategory.RELATIONSHIP -> R.string.home_category_score_relationship
            FortuneCategory.LOVE -> R.string.home_category_score_love
            FortuneCategory.ACHIEVEMENT -> R.string.home_category_score_achievement
            FortuneCategory.HEALTH -> R.string.home_category_score_health
            FortuneCategory.MONEY -> R.string.home_category_score_money
        },
    )
