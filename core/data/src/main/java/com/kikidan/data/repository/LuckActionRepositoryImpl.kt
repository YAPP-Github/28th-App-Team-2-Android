package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteLuckActionDataSource
import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.repository.LuckActionRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class LuckActionRepositoryImpl
    @Inject
    constructor(
        private val remoteLuckActionDataSource: RemoteLuckActionDataSource,
    ) : LuckActionRepository {
        override suspend fun getTodayLuckActions(): Result<List<LuckAction>> =
            runCatchingCancellable { remoteLuckActionDataSource.getTodayLuckActions() }

        override suspend fun toggleAchievement(luckActionId: String): Result<LuckAction> =
            runCatchingCancellable { remoteLuckActionDataSource.patchAchievement(luckActionId) }
    }
