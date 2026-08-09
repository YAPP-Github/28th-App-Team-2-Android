package com.kikidan.domain.usecase

import com.kikidan.domain.model.fortune.LuckActionPage
import com.kikidan.domain.repository.FortuneRepository
import com.kikidan.domain.repository.LuckActionRepository
import java.time.LocalDate
import javax.inject.Inject

class GetLuckActionPageUseCase
    @Inject
    constructor(
        private val fortuneRepository: FortuneRepository,
        private val luckActionRepository: LuckActionRepository,
    ) {
        // null은 해당 날짜에 기록이 없다는 뜻이며 에러가 아니다.
        suspend operator fun invoke(date: LocalDate): Result<LuckActionPage?> =
            if (date == LocalDate.now()) {
                invokeToday()
            } else {
                fortuneRepository
                    .getFortuneRecordForDate(date)
                    .map { record -> record?.let { LuckActionPage(it.scores, it.actions) } }
            }

        private suspend fun invokeToday(): Result<LuckActionPage?> {
            val scores = fortuneRepository.getTodayFortuneScores().getOrElse { return Result.failure(it) }
            val actions = luckActionRepository.getTodayLuckActions().getOrElse { return Result.failure(it) }
            return Result.success(LuckActionPage(scores, actions))
        }
    }
