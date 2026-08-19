package com.kikidan.domain.usecase.saju

import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.SajuChartDetail
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class PartnerMansaeryeokDetail(
    val partner: PartnerSaju,
    val chart: SajuChartDetail,
)

class GetPartnerMansaeryeokDetailUseCase
    @Inject
    constructor(
        private val getPartnerSajuUseCase: GetPartnerSajuUseCase,
        private val getPartnerSajuChartDetailUseCase: GetPartnerSajuChartDetailUseCase,
    ) {
        suspend operator fun invoke(linkId: String): Result<PartnerMansaeryeokDetail> =
            coroutineScope {
                val partnerDeferred = async { getPartnerSajuUseCase(linkId) }
                val chartDeferred = async { getPartnerSajuChartDetailUseCase(linkId) }

                val partner = partnerDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                val chart = chartDeferred.await().getOrElse { return@coroutineScope Result.failure(it) }
                Result.success(PartnerMansaeryeokDetail(partner, chart))
            }
    }
