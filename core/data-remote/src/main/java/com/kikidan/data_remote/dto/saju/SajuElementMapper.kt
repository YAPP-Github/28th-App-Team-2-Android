package com.kikidan.data_remote.dto.saju

import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi

internal fun String.toCheonGan(): CheonGan =
    CheonGan.entries.find { it.name == this }
        ?: error("Unknown CheonGan code from server: $this")

internal fun String.toJiJi(): JiJi =
    JiJi.entries.find { it.name == this }
        ?: error("Unknown JiJi code from server: $this")
