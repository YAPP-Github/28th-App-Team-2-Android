package com.kikidan.domain.usecase.mypage

import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.user.User
import com.kikidan.domain.usecase.saju.GetSajuPaljaUseCase
import com.kikidan.domain.usecase.user.GetUserUseCase
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
        suspend operator fun invoke(): Result<MyPageInfo> {
            val user = getUserUseCase().getOrElse { return Result.failure(it) }
            val sajuPalja = getSajuPaljaUseCase().getOrElse { return Result.failure(it) }
            return Result.success(MyPageInfo(user, sajuPalja))
        }
    }
