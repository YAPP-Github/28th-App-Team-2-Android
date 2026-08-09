package com.kikidan.data.fake

import com.kikidan.data.datasource.RemoteLuckActionDataSource
import com.kikidan.domain.model.fortune.LuckAction

class FakeRemoteLuckActionDataSource : RemoteLuckActionDataSource {
    var actions: List<LuckAction> = emptyList()
    var throwOnGetTodayLuckActions: Throwable? = null

    var toggled: LuckAction = LuckAction("1", com.kikidan.domain.model.fortune.FortuneCategory.LOVE, "제목", true)
    var throwOnPatchAchievement: Throwable? = null
    var lastPatchedId: String? = null

    override suspend fun getTodayLuckActions(): List<LuckAction> {
        throwOnGetTodayLuckActions?.let { throw it }
        return actions
    }

    override suspend fun patchAchievement(luckActionId: String): LuckAction {
        lastPatchedId = luckActionId
        throwOnPatchAchievement?.let { throw it }
        return toggled
    }
}
