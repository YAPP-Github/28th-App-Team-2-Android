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
