package com.kikidan.domain.usecase.mypage

import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.user.User
import com.kikidan.domain.usecase.saju.GetSajuPaljaUseCase
import com.kikidan.domain.usecase.user.GetUserUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class MyPageInfo(
    val user: User,
    val sajuPalja: SajuPalja,
)

class GetMyPageInfoUseCase
    @Inject
    constructor(
        private val getUserUseCase: GetUserUseCase,
        private val getSajuPaljaUseCase: GetSajuPaljaUseCase,
    ) {
        suspend operator fun invoke(): Result<MyPageInfo> =
            coroutineScope {
                val userDeferred = async { getUserUseCase() }
                val sajuPaljaDeferred = async { getSajuPaljaUseCase() }

                val user = userDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                val sajuPalja = sajuPaljaDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                Result.success(MyPageInfo(user, sajuPalja))
            }
    }
