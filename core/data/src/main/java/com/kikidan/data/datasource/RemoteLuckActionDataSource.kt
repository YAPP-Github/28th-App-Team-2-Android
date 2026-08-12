package com.kikidan.data.datasource

import com.kikidan.domain.model.fortune.LuckAction

interface RemoteLuckActionDataSource {
    suspend fun getTodayLuckActions(): List<LuckAction>

    suspend fun patchAchievement(luckActionId: String): LuckAction
}
