package com.kikidan.data_remote.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

internal fun String.toInstantOrThrow(): Instant =
    runCatching { Instant.parse(this) }
        .getOrElse { LocalDateTime.parse(this).atZone(KST).toInstant() }

private val KST: ZoneId = ZoneId.of("Asia/Seoul")
