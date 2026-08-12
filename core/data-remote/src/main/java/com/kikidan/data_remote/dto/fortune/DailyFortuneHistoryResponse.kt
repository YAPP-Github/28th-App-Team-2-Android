package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DailyFortuneHistoryResponse(
    val id: String,
    val fortuneDate: String,
    val luckActions: List<LuckActionSummaryResponse> = emptyList(),
)

internal fun DailyFortuneHistoryResponse.toDomain(): DailyFortuneHistoryEntry =
    DailyFortuneHistoryEntry(
        id = id,
        fortuneDate = LocalDate.parse(fortuneDate),
        actions = luckActions.map { it.toDomain() },
    )
