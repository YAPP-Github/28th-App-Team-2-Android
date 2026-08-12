package com.kikidan.domain.fake

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.repository.LuckActionRepository

class FakeLuckActionRepository : LuckActionRepository {
    var actionsResult: Result<List<LuckAction>> = Result.failure(NotImplementedError())
    var toggleResult: Result<LuckAction> = Result.failure(NotImplementedError())
    var lastToggledId: String? = null

    override suspend fun getTodayLuckActions(): Result<List<LuckAction>> = actionsResult

    override suspend fun toggleAchievement(luckActionId: String): Result<LuckAction> {
        lastToggledId = luckActionId
        return toggleResult
    }
}
