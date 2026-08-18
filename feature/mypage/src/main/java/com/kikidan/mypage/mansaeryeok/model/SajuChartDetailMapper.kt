package com.kikidan.mypage.mansaeryeok.model

import com.kikidan.designsystem.R
import com.kikidan.domain.model.saju.FiveElementCount
import com.kikidan.domain.model.saju.SajuChartDetail
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.saju.SajuPillarType
import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.saju.TenGod as DomainTenGod
import com.kikidan.domain.model.saju.TwelveSinsal as DomainTwelveSinsal
import com.kikidan.domain.model.saju.TwelveUnseong as DomainTwelveUnseong

private val PillarDisplayOrder =
    listOf(SajuPillarType.HOUR, SajuPillarType.DAY, SajuPillarType.MONTH, SajuPillarType.YEAR)

fun DomainTenGod.toUi(): TenGod = TenGod.valueOf(name)

fun DomainTwelveUnseong.toUi(): TwelveUnseong = TwelveUnseong.valueOf(name)

fun DomainTwelveSinsal.toUi(): TwelveSinsal = TwelveSinsal.valueOf(name)

private fun SajuPillarType.labelRes(): Int =
    when (this) {
        SajuPillarType.YEAR -> R.string.mansaeryeok_pillar_year
        SajuPillarType.MONTH -> R.string.mansaeryeok_pillar_month
        SajuPillarType.DAY -> R.string.mansaeryeok_pillar_day
        SajuPillarType.HOUR -> R.string.mansaeryeok_pillar_hour
    }

private fun SajuPillarType.periodLabelRes(): Int =
    when (this) {
        SajuPillarType.YEAR -> R.string.mansaeryeok_period_early
        SajuPillarType.MONTH -> R.string.mansaeryeok_period_youth
        SajuPillarType.DAY -> R.string.mansaeryeok_period_prime
        SajuPillarType.HOUR -> R.string.mansaeryeok_period_late
    }

private fun FiveElementCount.toStatus(): FiveElementStatus =
    when {
        percentage < 15.0 -> FiveElementStatus.LACKING
        percentage > 30.0 -> FiveElementStatus.ABUNDANT
        else -> FiveElementStatus.MODERATE
    }

private fun SajuPillarDetail.toUi(): com.kikidan.mypage.mansaeryeok.model.SajuPillarDetail =
    com.kikidan.mypage.mansaeryeok.model.SajuPillarDetail(
        pillar =
            com.kikidan.domain.model.saju
                .SajuPillar(cheonGan = cheonGan, jiJi = jiJi),
        pillarLabelRes = pillarType.labelRes(),
        periodLabelRes = pillarType.periodLabelRes(),
        topTenGod = stemTenGod.toUi(),
        bottomTenGod = branchTenGod.toUi(),
        hiddenStem = hiddenStems.joinToString(separator = "") { it.displayName },
        twelveUnseong = twelveUnseong.toUi(),
        twelveSinsal = twelveSinsal.toUi(),
    )

fun SajuChartDetail.toUiModel(user: User): MansaeryeokDetailUiModel =
    MansaeryeokDetailUiModel(
        user = user,
        pillars =
            PillarDisplayOrder.mapNotNull { type ->
                pillars.firstOrNull { it.pillarType == type }?.toUi()
            },
        fiveElements =
            fiveElements.map { entry ->
                FiveElementDistribution(ohaeng = entry.ohaeng, count = entry.count, status = entry.toStatus())
            },
    )
