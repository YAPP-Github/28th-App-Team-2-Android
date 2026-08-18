package com.kikidan.domain.fake

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail
import com.kikidan.domain.repository.LuckActionRepository

class FakeLuckActionRepository : LuckActionRepository {
    var actionsResult: Result<List<LuckAction>> = Result.failure(NotImplementedError())
    var toggleResult: Result<LuckAction> = Result.failure(NotImplementedError())
    var lastToggledId: String? = null
    var luckActionDetailResult: Result<LuckActionDetail> = Result.failure(NotImplementedError())
    var lastRequestedDetailId: String? = null

    override suspend fun getTodayLuckActions(): Result<List<LuckAction>> = actionsResult

    override suspend fun toggleAchievement(luckActionId: String): Result<LuckAction> {
        lastToggledId = luckActionId
        return toggleResult
    }

    override suspend fun getLuckActionDetail(luckActionId: String): Result<LuckActionDetail> {
        lastRequestedDetailId = luckActionId
        return luckActionDetailResult
    }
}
