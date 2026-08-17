package com.kikidan.chat.util

import com.kikidan.designsystem.R
import com.kikidan.domain.model.chat.ChatCategory

internal fun ChatCategory.toCharacterResourceId() =
    when (this) {
        ChatCategory.LOVE -> R.drawable.img_todak_love_luck
        ChatCategory.MONEY -> R.drawable.img_todak_money_luck
        ChatCategory.ACHIEVEMENT -> R.drawable.img_todak_achievement_luck
        ChatCategory.HEALTH -> R.drawable.img_todak_health_luck
        ChatCategory.RELATIONSHIP -> R.drawable.img_todak_relationship_luck
    }

internal fun ChatCategory?.toSuggestionAnswerResId() =
    when (this) {
        ChatCategory.RELATIONSHIP -> R.string.chat_suggestion_answer_relationship
        ChatCategory.LOVE -> R.string.chat_suggestion_answer_love
        ChatCategory.ACHIEVEMENT -> R.string.chat_suggestion_answer_achievement
        ChatCategory.MONEY -> R.string.chat_suggestion_answer_money
        ChatCategory.HEALTH -> R.string.chat_suggestion_answer_health
        null -> R.string.chat_suggestion_answer_other
    }
