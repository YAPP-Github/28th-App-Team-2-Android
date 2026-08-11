package com.kikidan.sajucontents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunChip
import com.kikidan.designsystem.component.TodakunSelectField
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.designsystem.R
import com.kikidan.sajucontents.component.DateSelectBottomSheet
import com.kikidan.sajucontents.model.DateFortuneState
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateSummaryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d(E)", Locale.KOREAN)

@Composable
internal fun DateFortuneInputScreen(
    state: DateFortuneState,
    onPurposeSelect: (DayFortunePurpose) -> Unit,
    onOpenDateSheet: () -> Unit,
    onCloseDateSheet: () -> Unit,
    onDateToggle: (LocalDate) -> Unit,
    onDateRemove: (LocalDate) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            TodakunSubHeader(
                title = stringResource(R.string.date_fortune_header_title),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.date_fortune_title),
                style = TodakunTypography.heading3Bold,
                color = TodakunColor.gray975,
            )

            Spacer(modifier = Modifier.height(20.dp))

            PurposeChipGroup(
                selectedPurpose = state.selectedPurpose,
                onPurposeSelect = onPurposeSelect,
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.date_fortune_dates_label),
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.gray975,
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text =
                        stringResource(
                            id = R.string.date_fortune_dates_sub_label,
                            DateFortuneDefaults.MAX_TARGET_DATES,
                        ),
                    style = TodakunTypography.body3Regular,
                    color = TodakunColor.gray700,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            TodakunSelectField(
                value = state.selectedDates.joinToString(", ") { it.format(DateSummaryFormatter) },
                onClick = onOpenDateSheet,
                onClear = onReset,
                placeholder = stringResource(id = R.string.date_fortune_dates_placeholder),
                expanded = state.isSheetVisible,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 14.dp,
                    ).align(Alignment.BottomCenter),
        ) {
            PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.date_fortune_cta),
                onClick = onSubmit,
                size = TodakunButtonSize.Large,
                enabled = state.canSubmit,
            )
        }

        if (state.isSheetVisible) {
            DateSelectBottomSheet(
                selectedDates = state.selectedDates,
                onDateToggle = onDateToggle,
                onDateRemove = onDateRemove,
                onReset = onReset,
                onConfirm = onCloseDateSheet,
                onDismissRequest = onCloseDateSheet,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PurposeChipGroup(
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


@Preview(showBackground = true)
@Composable
private fun DateFortuneInputScreenPreview() {
    TodakunTheme {
        DateFortuneInputScreen(
            state =
                DateFortuneState(
                    selectedPurpose = DayFortunePurpose.TRAVEL,
                    selectedDates = persistentListOf(LocalDate.now()),
                ),
            onPurposeSelect = {},
            onOpenDateSheet = {},
            onCloseDateSheet = {},
            onDateToggle = {},
            onDateRemove = {},
            onReset = {},
            onSubmit = {},
        )
    }
}
