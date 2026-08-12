package com.kikidan.domain.repository

import com.kikidan.domain.model.fortune.LuckAction

interface LuckActionRepository {
    suspend fun getTodayLuckActions(): Result<List<LuckAction>>

    suspend fun toggleAchievement(luckActionId: String): Result<LuckAction>
}
