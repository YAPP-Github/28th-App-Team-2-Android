package com.kikidan.data_remote.dto.common

import com.kikidan.domain.model.user.BirthTime

internal fun BirthTime.toApiValue(): String =
    when (this) {
        BirthTime.JA -> "JASI"
        BirthTime.CHUK -> "CHUKSI"
        BirthTime.IN -> "INSI"
        BirthTime.MYO -> "MYOSI"
        BirthTime.JIN -> "JINSI"
        BirthTime.SA -> "SASI"
        BirthTime.O -> "OSI"
        BirthTime.MI -> "MISI"
        BirthTime.SIN -> "SINSI"
        BirthTime.YU -> "YUSI"
        BirthTime.SUL -> "SULSI"
        BirthTime.HAE -> "HAESI"
        BirthTime.UNKNOWN -> "UNKNOWN"
    }

internal fun String.toBirthTime(): BirthTime =
    when (this) {
        "JASI" -> BirthTime.JA
        "CHUKSI" -> BirthTime.CHUK
        "INSI" -> BirthTime.IN
        "MYOSI" -> BirthTime.MYO
        "JINSI" -> BirthTime.JIN
        "SASI" -> BirthTime.SA
        "OSI" -> BirthTime.O
        "MISI" -> BirthTime.MI
        "SINSI" -> BirthTime.SIN
        "YUSI" -> BirthTime.YU
        "SULSI" -> BirthTime.SUL
        "HAESI" -> BirthTime.HAE
        "UNKNOWN" -> BirthTime.UNKNOWN
        else -> error("Unknown birthTime code from server: $this")
    }
