package com.kikidan.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunCheckbox
import com.kikidan.designsystem.component.TodakunSelectBox
import com.kikidan.designsystem.component.TodakunSelectField
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.onboarding.OnboardingSheet
import com.kikidan.onboarding.OnboardingStep
import com.kikidan.onboarding.R
import com.kikidan.onboarding.component.OnboardingScaffold
import java.time.LocalDate
import com.kikidan.designsystem.R as DesignSystemR

@Composable
internal fun BirthInfoScreen(
    gender: Gender?,
    calendarType: DateType?,
    birthDate: LocalDate?,
    birthTime: BirthTime?,
    openedSheet: OnboardingSheet?,
    canProceed: Boolean,
    onGenderSelect: (Gender) -> Unit,
    onCalendarTypeSelect: (DateType) -> Unit,
    onSheetOpen: (OnboardingSheet) -> Unit,
    onBirthDateClear: () -> Unit,
    onBirthTimeClear: () -> Unit,
    onBirthTimeUnknownChange: (Boolean) -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title =
        buildAnnotatedString {
            append(stringResource(R.string.onboarding_birth_title1))
            withStyle(style = SpanStyle(color = TodakunColor.primary700)) {
                append(stringResource(R.string.onboarding_birth_title_primary))
            }
            append(stringResource(R.string.onboarding_birth_title2))
        }
    OnboardingScaffold(
        progress = OnboardingStep.BIRTH_INFO.progress,
        title = title,
        ctaText = stringResource(id = R.string.onboarding_next),
        ctaEnabled = canProceed,
        onCtaClick = onNextClick,
        onBackClick = onBackClick,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
            LabeledField(label = stringResource(id = R.string.onboarding_birth_gender_label)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TodakunSelectBox(
                        text = stringResource(id = R.string.onboarding_birth_gender_male),
                        selected = gender == Gender.MALE,
                        onClick = { onGenderSelect(Gender.MALE) },
                        modifier = Modifier.weight(1f),
                    )
                    TodakunSelectBox(
                        text = stringResource(id = R.string.onboarding_birth_gender_female),
                        selected = gender == Gender.FEMALE,
                        onClick = { onGenderSelect(Gender.FEMALE) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LabeledField(label = stringResource(id = R.string.onboarding_birth_calendar_label)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TodakunSelectBox(
                        text = stringResource(id = R.string.onboarding_birth_calendar_solar),
                        selected = calendarType == DateType.SOLAR,
                        onClick = { onCalendarTypeSelect(DateType.SOLAR) },
                        modifier = Modifier.weight(1f),
                    )
                    TodakunSelectBox(
                        text = stringResource(id = R.string.onboarding_birth_calendar_lunar),
                        selected = calendarType == DateType.LUNAR,
                        onClick = { onCalendarTypeSelect(DateType.LUNAR) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            LabeledField(label = stringResource(id = R.string.onboarding_birth_date_label)) {
                TodakunSelectField(
                    value = birthDate.formatted(),
                    onClick = { onSheetOpen(OnboardingSheet.BIRTH_DATE) },
                    placeholder = stringResource(id = R.string.onboarding_birth_date_placeholder),
                    expanded = openedSheet == OnboardingSheet.BIRTH_DATE,
                    onClear = onBirthDateClear,
                )
            }

            LabeledField(label = stringResource(id = R.string.onboarding_birth_time_label)) {
                TodakunSelectField(
                    value = birthTime.formatted(),
                    onClick = { onSheetOpen(OnboardingSheet.BIRTH_TIME) },
                    placeholder = stringResource(id = R.string.onboarding_birth_time_placeholder),
                    expanded = openedSheet == OnboardingSheet.BIRTH_TIME,
                    onClear = onBirthTimeClear,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TodakunCheckbox(
                        checked = birthTime == BirthTime.UNKNOWN,
                        onCheckedChange = onBirthTimeUnknownChange,
                    )
                    Text(
                        text = stringResource(id = R.string.onboarding_birth_time_unknown),
                        style = TodakunTypography.body3Medium,
                        color = TodakunColor.gray975,
                    )
                }
                if (birthTime == BirthTime.UNKNOWN) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = R.string.onboarding_birth_time_unknown_hint),
                        style = TodakunTypography.caption1Regular,
                        color = TodakunColor.sky600,
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TodakunTypography.body1Bold,
            color = TodakunColor.black,
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun LocalDate?.formatted(): String =
    this
        ?.let {
            stringResource(id = R.string.onboarding_birth_date_format, it.year, it.monthValue, it.dayOfMonth)
        }.orEmpty()

/**
 * 지시(地支)의 표시 문자열은 휠피커가 쓰는 `core:designsystem` 문자열 배열이 원본이다.
 * `BirthTime`은 12지시 뒤에 `UNKNOWN`이 이어지는 순서라 앞 12개 ordinal이 그 배열 순서와 1:1로 대응한다.
 * `UNKNOWN`(ordinal 12)이나 미선택(null)은 배열 범위를 벗어나 빈 문자열로 떨어진다.
 */
@Composable
private fun BirthTime?.formatted(): String {
    val labels = stringArrayResource(id = DesignSystemR.array.wheel_picker_saju_birth_times)
    return this?.let { labels.getOrNull(it.ordinal) }.orEmpty()
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun BirthInfoScreenPreview() {
    TodakunTheme {
        BirthInfoScreen(
            gender = Gender.FEMALE,
            calendarType = DateType.SOLAR,
            birthDate = LocalDate.of(1999, 2, 13),
            birthTime = BirthTime.JA,
            openedSheet = null,
            canProceed = true,
            onGenderSelect = {},
            onCalendarTypeSelect = {},
            onSheetOpen = {},
            onBirthDateClear = {},
            onBirthTimeClear = {},
            onBirthTimeUnknownChange = {},
            onNextClick = {},
            onBackClick = {},
        )
    }
}
