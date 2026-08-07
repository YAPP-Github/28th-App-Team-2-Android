package com.kikidan.luckaction.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadgeType
import com.kikidan.domain.model.fortune.FortuneCategory

@Composable
internal fun FortuneCategory.scoreLabel(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_score_relationship
            FortuneCategory.LOVE -> R.string.luck_action_score_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_score_achievement
            FortuneCategory.MONEY -> R.string.luck_action_score_money
            FortuneCategory.HEALTH -> R.string.luck_action_score_health
        },
    )

@Composable
internal fun FortuneCategory.badgeLabel(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_badge_relationship
            FortuneCategory.LOVE -> R.string.luck_action_badge_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_badge_achievement
            FortuneCategory.MONEY -> R.string.luck_action_badge_money
            FortuneCategory.HEALTH -> R.string.luck_action_badge_health
        },
    )

internal fun FortuneCategory.badgeType(): TodakunBadgeType =
    when (this) {
        FortuneCategory.RELATIONSHIP -> TodakunBadgeType.Purple
        FortuneCategory.LOVE -> TodakunBadgeType.Pink
        FortuneCategory.ACHIEVEMENT -> TodakunBadgeType.Green
        FortuneCategory.MONEY -> TodakunBadgeType.Blue
        FortuneCategory.HEALTH -> TodakunBadgeType.Yellow
    }

@Composable
internal fun FortuneCategory.completeMessage(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_complete_message_relationship
            FortuneCategory.LOVE -> R.string.luck_action_complete_message_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_complete_message_achievement
            FortuneCategory.MONEY -> R.string.luck_action_complete_message_money
            FortuneCategory.HEALTH -> R.string.luck_action_complete_message_health
        },
    )
