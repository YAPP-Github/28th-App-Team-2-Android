package com.kikidan.data.repository

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.repository.SajuRepository
import javax.inject.Inject

class FakeSajuRepository
    @Inject
    constructor() : SajuRepository {
        override suspend fun getSajuPalja(): Result<SajuPalja> =
            Result.success(
                SajuPalja(
                    yearPillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                    monthPillar = SajuPillar(CheonGan.GI, JiJi.SA),
                    dayPillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                    hourPillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                ),
            )
    }
