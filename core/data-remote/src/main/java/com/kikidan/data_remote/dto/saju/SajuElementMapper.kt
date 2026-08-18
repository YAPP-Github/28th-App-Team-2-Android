package com.kikidan.data_remote.dto.saju

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.TenGod
import com.kikidan.domain.model.saju.TwelveSinsal
import com.kikidan.domain.model.saju.TwelveUnseong

internal fun String.toCheonGan(): CheonGan =
    CheonGan.entries.find { it.name == this }
        ?: error("Unknown CheonGan code from server: $this")

internal fun String.toJiJi(): JiJi =
    JiJi.entries.find { it.name == this }
        ?: error("Unknown JiJi code from server: $this")

internal fun String.toOhaeng(): Ohaeng =
    when (this) {
        "WOOD" -> Ohaeng.MOK
        "FIRE" -> Ohaeng.HWA
        "EARTH" -> Ohaeng.TO
        "METAL" -> Ohaeng.GEUM
        "WATER" -> Ohaeng.SU
        else -> Ohaeng.entries.find { it.name == this } ?: error("Unknown Ohaeng code from server: $this")
    }

internal fun String.toTenGod(): TenGod =
    TenGod.entries.find { it.name == this }
        ?: error("Unknown TenGod code from server: $this")

internal fun String.toTwelveUnseong(): TwelveUnseong =
    TwelveUnseong.entries.find { it.name == this }
        ?: error("Unknown TwelveUnseong code from server: $this")

internal fun String.toTwelveSinsal(): TwelveSinsal =
    TwelveSinsal.entries.find { it.name == this }
        ?: error("Unknown TwelveSinsal code from server: $this")
