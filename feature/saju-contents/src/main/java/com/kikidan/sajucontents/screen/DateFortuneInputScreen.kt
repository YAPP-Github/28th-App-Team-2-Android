package com.kikidan.sajucontents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunProgressBar
import com.kikidan.designsystem.component.TodakunSelectField
import com.kikidan.designsystem.component.TodakunSnackbar
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.component.DateSelectBottomSheet
import com.kikidan.sajucontents.component.GenderSelector
import com.kikidan.sajucontents.component.PurposeChipGroup
import com.kikidan.sajucontents.model.DateFortuneState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// 입력·결과 화면 모두 210/316(≈66%)로 동일해 정확한 단계 수가 불명확하다 (설계 문서 Q10).
// progress를 파라미터로 열어 둔 TodakunProgressBar 그대로 사용하고 값만 로컬 상수로 둔다.
private const val INPUT_PROGRESS = 210f / 316f
private const val TOAST_DURATION_MS = 2000L
private val DateSummaryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d(E)", Locale.KOREAN)

@Composable
internal fun DateFortuneInputScreen(
    state: DateFortuneState,
    toastMessage: String?,
    onToastDismiss: () -> Unit,
    onBackClick: () -> Unit,
    onPurposeSelect: (DayFortunePurpose) -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onOpenDateSheet: () -> Unit,
    onCloseDateSheet: () -> Unit,
    onDateToggle: (LocalDate) -> Unit,
    onDateRemove: (LocalDate) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(TodakunColor.white)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TodakunProgressBar(
                progress = INPUT_PROGRESS,
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(id = R.string.date_fortune_title),
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.gray975,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                )
                GenderSelector(
                    selectedGender = state.selectedGender,
                    onGenderSelect = onGenderSelect,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                PurposeChipGroup(
                    selectedPurpose = state.selectedPurpose,
                    onPurposeSelect = onPurposeSelect,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
                Text(
                    text = stringResource(id = R.string.date_fortune_dates_label, DateFortuneDefaults.MAX_TARGET_DATES),
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.gray700,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                TodakunSelectField(
                    value = state.selectedDates.joinToString(", ") { it.format(DateSummaryFormatter) },
                    onClick = onOpenDateSheet,
                    placeholder = stringResource(id = R.string.date_fortune_dates_placeholder),
                    expanded = state.isSheetVisible,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                ) {
                    PrimaryButton(
                        text = stringResource(id = R.string.date_fortune_cta),
                        onClick = onSubmit,
                        size = TodakunButtonSize.Large,
                        enabled = state.canSubmit,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TodakunColor.white,
                            strokeWidth = 2.dp,
                        )
                    }
                }
            }
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

        if (toastMessage != null) {
            val currentOnToastDismiss by rememberUpdatedState(onToastDismiss)
            LaunchedEffect(toastMessage) {
                delay(TOAST_DURATION_MS)
                currentOnToastDismiss()
            }
            TodakunSnackbar(
                text = toastMessage,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
            )
        }
    }
}

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
            toastMessage = null,
            onToastDismiss = {},
            onBackClick = {},
            onPurposeSelect = {},
            onGenderSelect = {},
            onOpenDateSheet = {},
            onCloseDateSheet = {},
            onDateToggle = {},
            onDateRemove = {},
            onReset = {},
            onSubmit = {},
        )
    }
}
