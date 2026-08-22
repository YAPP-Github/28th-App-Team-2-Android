package com.kikidan.domain.repository

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail

interface LuckActionRepository {
    suspend fun getTodayLuckActions(): Result<List<LuckAction>>

    suspend fun toggleAchievement(luckActionId: String): Result<LuckAction>

    suspend fun getLuckActionDetail(luckActionId: String): Result<LuckActionDetail>
}
