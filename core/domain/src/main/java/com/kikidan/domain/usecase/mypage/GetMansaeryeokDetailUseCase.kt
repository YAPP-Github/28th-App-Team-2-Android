package com.kikidan.domain.usecase.mypage

import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.user.User
import com.kikidan.domain.usecase.saju.GetSajuChartDetailUseCase
import com.kikidan.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class MansaeryeokDetail(
    val user: User,
    val chart: SajuChartDetail,
)

class GetMansaeryeokDetailUseCase
    @Inject
    constructor(
        private val getUserUseCase: GetUserUseCase,
        private val getSajuChartDetailUseCase: GetSajuChartDetailUseCase,
    ) {
        suspend operator fun invoke(): Result<MansaeryeokDetail> =
            coroutineScope {
                val userDeferred = async { getUserUseCase() }
                val chartDeferred = async { getSajuChartDetailUseCase() }

                val user = userDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                val chart = chartDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                Result.success(MansaeryeokDetail(user, chart))
            }
    }
