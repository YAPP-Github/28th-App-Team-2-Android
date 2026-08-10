package com.kikidan.sajucontents.dayfortune.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunChip
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.sajucontents.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurposeChipGroup(
    selectedPurpose: DayFortunePurpose?,
    onPurposeSelect: (DayFortunePurpose) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DayFortunePurpose.entries.forEach { purpose ->
            TodakunChip(
                label = purpose.label(),
                selected = purpose == selectedPurpose,
                onClick = { onPurposeSelect(purpose) },
            )
        }
    }
}

@Composable
fun DayFortunePurpose.label(): String =
    stringResource(
        id =
            when (this) {
                DayFortunePurpose.CONTRACT_MOVING -> R.string.date_fortune_purpose_contract_moving
                DayFortunePurpose.BUSINESS_OPENING -> R.string.date_fortune_purpose_business_opening
                DayFortunePurpose.TRAVEL -> R.string.date_fortune_purpose_travel
                DayFortunePurpose.CONFESSION_DATING -> R.string.date_fortune_purpose_confession_dating
                DayFortunePurpose.EXAM_INTERVIEW -> R.string.date_fortune_purpose_exam_interview
            },
    )
