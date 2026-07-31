package com.kikidan.domain.usecase.saju

import com.kikidan.domain.di.Fake
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.repository.SajuRepository
import javax.inject.Inject

class GetSajuPaljaUseCase
    @Inject
    constructor(
        @Fake private val sajuRepository: SajuRepository,
    ) {
        suspend operator fun invoke(): Result<SajuPalja> = sajuRepository.getSajuPalja()
    }
