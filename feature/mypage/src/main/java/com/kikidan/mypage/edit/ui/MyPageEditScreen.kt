package com.kikidan.mypage.edit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunCheckbox
import com.kikidan.designsystem.component.TodakunProgressIndicator
import com.kikidan.designsystem.component.TodakunSelectBox
import com.kikidan.designsystem.component.TodakunSelectField
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.component.wheelpicker.BirthDateState
import com.kikidan.designsystem.component.wheelpicker.BirthDateWheelPicker
import com.kikidan.designsystem.component.wheelpicker.SajuBirthTimeWheelPicker
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.mypage.edit.model.LifeStatus
import com.kikidan.mypage.edit.model.MyPageEditUiModel
import com.kikidan.mypage.edit.model.MyPageEditUiState
import com.kikidan.mypage.edit.model.RelationshipStatus
import com.kikidan.mypage.edit.ui.component.CurrentSituationBottomSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MyPageEditScreen(
    uiState: MyPageEditUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onGenderSelect: (Gender) -> Unit = {},
    onDateTypeSelect: (DateType) -> Unit = {},
    onBirthDateChange: (LocalDate) -> Unit = {},
    onBirthTimeChange: (BirthTime) -> Unit = {},
    onCurrentSituationChange: (LifeStatus, RelationshipStatus) -> Unit = { _, _ -> },
    onSaveClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.mypage_edit_title),
            onBackClick = onBackClick,
        )

        when (uiState) {
            is MyPageEditUiState.Loading -> {
                TodakunProgressIndicator(modifier = Modifier.weight(1f))
            }

            is MyPageEditUiState.Fail -> {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
            }

            is MyPageEditUiState.Success -> {
                MyPageEditContent(
                    model = uiState.model,
                    isSaving = uiState.isSaving,
                    onGenderSelect = onGenderSelect,
                    onDateTypeSelect = onDateTypeSelect,
                    onBirthDateChange = onBirthDateChange,
                    onBirthTimeChange = onBirthTimeChange,
                    onCurrentSituationChange = onCurrentSituationChange,
                    onSaveClick = onSaveClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MyPageEditContent(
    model: MyPageEditUiModel,
    isSaving: Boolean,
    onGenderSelect: (Gender) -> Unit,
    onDateTypeSelect: (DateType) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onBirthTimeChange: (BirthTime) -> Unit,
    onCurrentSituationChange: (LifeStatus, RelationshipStatus) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showBirthDatePicker by remember { mutableStateOf(false) }
    var showBirthTimePicker by remember { mutableStateOf(false) }
    var showCurrentSituationSheet by remember { mutableStateOf(false) }
    val birthTimeItems = stringArrayResource(R.array.wheel_picker_saju_birth_times)
    val birthTimeDisplayText =
        if (model.birthTime == BirthTime.UNKNOWN) {
            ""
        } else {
            birthTimeItems.getOrElse(model.birthTime.ordinal) { "" }
        }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            EditField(label = stringResource(R.string.mypage_edit_gender_label)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TodakunSelectBox(
                        text = stringResource(R.string.gender_male),
                        selected = model.gender == Gender.MALE,
                        onClick = { onGenderSelect(Gender.MALE) },
                        modifier = Modifier.weight(1f),
                    )
                    TodakunSelectBox(
                        text = stringResource(R.string.gender_female),
                        selected = model.gender == Gender.FEMALE,
                        onClick = { onGenderSelect(Gender.FEMALE) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            EditField(label = stringResource(R.string.mypage_edit_date_type_label)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TodakunSelectBox(
                        text = stringResource(R.string.date_type_solar),
                        selected = model.dateType == DateType.SOLAR,
                        onClick = { onDateTypeSelect(DateType.SOLAR) },
                        modifier = Modifier.weight(1f),
                    )
                    TodakunSelectBox(
                        text = stringResource(R.string.date_type_lunar),
                        selected = model.dateType == DateType.LUNAR,
                        onClick = { onDateTypeSelect(DateType.LUNAR) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            EditField(label = stringResource(R.string.mypage_edit_birth_date_label)) {
                TodakunSelectField(
                    value = model.birthDate.format(BirthDateFormatter),
                    onClick = { showBirthDatePicker = true },
                )
            }

            EditField(label = stringResource(R.string.mypage_edit_birth_time_label)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TodakunSelectField(
                        value = birthTimeDisplayText,
                        onClick = {
                            if (model.birthTime != BirthTime.UNKNOWN) {
                                showBirthTimePicker = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(TodakunColor.gray25)
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TodakunCheckbox(
                            checked = model.birthTime == BirthTime.UNKNOWN,
                            onCheckedChange = { checked ->
                                onBirthTimeChange(if (checked) BirthTime.UNKNOWN else BirthTime.JA)
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.mypage_edit_birth_time_unknown),
                            style = TodakunTypography.body2Medium,
                            color = TodakunColor.gray975,
                        )
                    }
                }
            }

            EditField(label = stringResource(R.string.mypage_edit_current_situation_label)) {
                TodakunSelectField(
                    value =
                        stringResource(
                            R.string.mypage_current_situation_format,
                            stringResource(model.lifeStatus.labelRes),
                            stringResource(model.relationshipStatus.labelRes),
                        ),
                    onClick = { showCurrentSituationSheet = true },
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.mypage_edit_save_button),
            onClick = onSaveClick,
            size = TodakunButtonSize.Large,
            isLoading = isSaving,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showBirthDatePicker) {
        var birthDateState by
            remember(model.birthDate) {
                mutableStateOf(BirthDateState.of(model.birthDate, 1900..LocalDate.now().year))
            }
        BirthDateWheelPicker(
            birthDateState = birthDateState,
            onBirthDateChange = { birthDateState = it },
            onSaveClick = {
                onBirthDateChange(LocalDate.of(birthDateState.year, birthDateState.month, birthDateState.day))
                showBirthDatePicker = false
            },
            onDismissRequest = { showBirthDatePicker = false },
        )
    }

    if (showBirthTimePicker) {
        var selectedLabel by remember(model.birthTime) { mutableStateOf(birthTimeDisplayText) }
        SajuBirthTimeWheelPicker(
            initialSelectedIndex = model.birthTime.ordinal.coerceIn(birthTimeItems.indices),
            onSajuBirthTimeChange = { selectedLabel = it },
            onSaveClick = {
                val index = birthTimeItems.indexOf(selectedLabel)
                if (index >= 0) {
                    onBirthTimeChange(BirthTime.entries[index])
                }
                showBirthTimePicker = false
            },
            onDismissRequest = { showBirthTimePicker = false },
        )
    }

    if (showCurrentSituationSheet) {
        CurrentSituationBottomSheet(
            onSaveClick = { life, relationship ->
                onCurrentSituationChange(life, relationship)
                showCurrentSituationSheet = false
            },
            onDismissRequest = { showCurrentSituationSheet = false },
            initialLifeStatus = model.lifeStatus,
            initialRelationshipStatus = model.relationshipStatus,
        )
    }
}

@Composable
private fun EditField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        EditFieldLabel(text = label)
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun EditFieldLabel(text: String) {
    Text(
        text = text,
        style = TodakunTypography.body1Bold,
        color = TodakunColor.black,
    )
}

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")

@Preview(showBackground = true)
@Composable
private fun MyPageEditScreenPreview() {
    TodakunTheme {
        MyPageEditScreen(
            uiState =
                MyPageEditUiState.Success(
                    MyPageEditUiModel(
                        id = "preview",
                        name = "토닥이",
                        gender = Gender.FEMALE,
                        dateType = DateType.SOLAR,
                        birthDate = LocalDate.of(1999, 2, 13),
                        birthTime = BirthTime.SA,
                        lifeStatus = LifeStatus.OFFICE_WORKER,
                        relationshipStatus = RelationshipStatus.SINGLE,
                    ),
                ),
        )
    }
}
