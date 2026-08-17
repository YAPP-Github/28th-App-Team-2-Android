package com.kikidan.home

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail
import com.kikidan.domain.repository.LuckActionRepository
import kotlinx.coroutines.CompletableDeferred

class FakeLuckActionRepository : LuckActionRepository {
    var actionsResult: Result<List<LuckAction>> = Result.failure(NotImplementedError())
    var toggleResult: Result<LuckAction> = Result.failure(NotImplementedError())
    var luckActionDetailResult: Result<LuckActionDetail> = Result.failure(NotImplementedError())
    var detailDeferred: CompletableDeferred<Result<LuckActionDetail>>? = null

    override suspend fun getTodayLuckActions(): Result<List<LuckAction>> = actionsResult

    override suspend fun toggleAchievement(luckActionId: String): Result<LuckAction> = toggleResult

    override suspend fun getLuckActionDetail(luckActionId: String): Result<LuckActionDetail> =
        detailDeferred?.await() ?: luckActionDetailResult
}
