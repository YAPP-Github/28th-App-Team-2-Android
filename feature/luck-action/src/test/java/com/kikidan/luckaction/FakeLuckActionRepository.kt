package com.kikidan.luckaction

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail
import com.kikidan.domain.repository.LuckActionRepository

class FakeLuckActionRepository : LuckActionRepository {
    var actionsResult: Result<List<LuckAction>> = Result.failure(NotImplementedError())
    var toggleResult: Result<LuckAction> = Result.failure(NotImplementedError())
    var luckActionDetailResult: Result<LuckActionDetail> = Result.failure(NotImplementedError())

    override suspend fun getTodayLuckActions(): Result<List<LuckAction>> = actionsResult

    override suspend fun toggleAchievement(luckActionId: String): Result<LuckAction> = toggleResult

    override suspend fun getLuckActionDetail(luckActionId: String): Result<LuckActionDetail> = luckActionDetailResult
}
