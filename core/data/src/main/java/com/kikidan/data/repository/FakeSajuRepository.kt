package com.kikidan.data.repository

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.FiveElementCount
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.saju.SajuPillarType
import com.kikidan.domain.model.saju.TenGod
import com.kikidan.domain.model.saju.TwelveSinsal
import com.kikidan.domain.model.saju.TwelveUnseong
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

        override suspend fun getMySajuChartDetail(): Result<SajuChartDetail> =
            Result.success(
                SajuChartDetail(
                    dayMaster = CheonGan.GI,
                    pillars =
                        listOf(
                            SajuPillarDetail(
                                pillarType = SajuPillarType.YEAR,
                                cheonGan = CheonGan.JEONG,
                                jiJi = JiJi.CHUK,
                                stemTenGod = TenGod.PYEONIN,
                                branchTenGod = TenGod.BIGYEON,
                                hiddenStems = listOf(CheonGan.EUL, CheonGan.GYE, CheonGan.MU),
                                twelveUnseong = TwelveUnseong.YANG,
                                twelveSinsal = TwelveSinsal.HWAGAESAL,
                            ),
                            SajuPillarDetail(
                                pillarType = SajuPillarType.MONTH,
                                cheonGan = CheonGan.GYE,
                                jiJi = JiJi.MYO,
                                stemTenGod = TenGod.PYEONJAE,
                                branchTenGod = TenGod.BIGYEON,
                                hiddenStems = listOf(CheonGan.GYE, CheonGan.SIN, CheonGan.GI),
                                twelveUnseong = TwelveUnseong.GWANDAE,
                                twelveSinsal = TwelveSinsal.BANANSAL,
                            ),
                            SajuPillarDetail(
                                pillarType = SajuPillarType.DAY,
                                cheonGan = CheonGan.GI,
                                jiJi = JiJi.SA,
                                stemTenGod = TenGod.ILWON,
                                branchTenGod = TenGod.BIGYEON,
                                hiddenStems = listOf(CheonGan.MU, CheonGan.GYEONG, CheonGan.BYEONG),
                                twelveUnseong = TwelveUnseong.TAE,
                                twelveSinsal = TwelveSinsal.GEOPSAL,
                            ),
                            SajuPillarDetail(
                                pillarType = SajuPillarType.HOUR,
                                cheonGan = CheonGan.SIN,
                                jiJi = JiJi.MI,
                                stemTenGod = TenGod.SIKSIN,
                                branchTenGod = TenGod.BIGYEON,
                                hiddenStems = listOf(CheonGan.EUL, CheonGan.GYE, CheonGan.MU),
                                twelveUnseong = TwelveUnseong.YANG,
                                twelveSinsal = TwelveSinsal.HWAGAESAL,
                            ),
                        ),
                    fiveElements =
                        listOf(
                            FiveElementCount(Ohaeng.MOK, count = 1, percentage = 12.0),
                            FiveElementCount(Ohaeng.HWA, count = 2, percentage = 25.0),
                            FiveElementCount(Ohaeng.TO, count = 3, percentage = 38.0),
                            FiveElementCount(Ohaeng.GEUM, count = 1, percentage = 12.0),
                            FiveElementCount(Ohaeng.SU, count = 1, percentage = 13.0),
                        ),
                ),
            )
    }
