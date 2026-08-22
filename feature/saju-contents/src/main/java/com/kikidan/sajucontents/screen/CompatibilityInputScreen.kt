package com.kikidan.sajucontents.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunProgressIndicator
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.component.wheelpicker.TodakunWheelPicker
import com.kikidan.designsystem.component.wheelpicker.WheelPickerBottomSheet
import com.kikidan.designsystem.component.wheelpicker.WheelPickerColumnState
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus
import com.kikidan.domain.model.user.User
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.component.CompatibilitySajuPillars
import com.kikidan.sajucontents.model.CompatibilityInputState
import com.kikidan.sajucontents.model.CreateCompatibilityState
import com.kikidan.sajucontents.model.MyInfoLoadState
import com.kikidan.sajucontents.model.PartnerListState
import com.kikidan.sajucontents.model.PartnerPickerState
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kikidan.designsystem.R as DesignSystemR

@Composable
internal fun CompatibilityInputScreen(
    state: CompatibilityInputState,
    onBackClick: () -> Unit,
    onChangeMyInfo: () -> Unit,
    onOpenPartnerPicker: () -> Unit,
    onDismissPartnerPicker: () -> Unit,
    onAddNewPartner: () -> Unit,
    onSelectPartner: (String) -> Unit,
    onCheckCompatibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.compatibility_entry_title),
            onBackClick = onBackClick,
        )

        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .padding(bottom = CompatibilityEntryDefaults.ButtonAreaHeight),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = stringResource(R.string.compatibility_my_info_title),
                        style = TodakunTypography.body1Bold,
                        color = TodakunColor.gray975,
                    )
                    state.myUser?.let { user ->
                        PersonCard(
                            name = user.name,
                            gender = user.gender,
                            birth = user.birth,
                            pillars = state.myPillars,
                            onChangeClick = onChangeMyInfo,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.compatibility_partner_info_title),
                        style = TodakunTypography.body1Bold,
                        color = TodakunColor.gray975,
                    )
                    val partner = state.selectedPartner
                    if (partner == null) {
                        AddPartnerPrompt(onClick = onOpenPartnerPicker)
                    } else {
                        PersonCard(
                            name = partner.name,
                            gender = partner.gender,
                            birth = partner.birth,
                            pillars = state.selectedPartnerPillars,
                            onChangeClick = onOpenPartnerPicker,
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.compatibility_check_cta),
                    onClick = onCheckCompatibilityClick,
                    enabled = state.selectedPartner != null && state.createState !is CreateCompatibilityState.Loading,
                    isLoading = state.createState is CreateCompatibilityState.Loading,
                    size = TodakunButtonSize.Large,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        when (state.myInfoState) {
            is MyInfoLoadState.Loading -> TodakunProgressIndicator()
            null, is MyInfoLoadState.Success, is MyInfoLoadState.Failure -> Unit
        }
    }
    val selectedIndex = state.partnerPicker.partners.indexOf(state.selectedPartner)
    if (state.partnerPicker.isVisible) {
        PartnerPickerSheet(
            selectedIndex = if (selectedIndex == -1) 0 else selectedIndex,
            pickerState = state.partnerPicker,
            onDismissRequest = onDismissPartnerPicker,
            onAddNewPartner = onAddNewPartner,
            onSelectPartner = onSelectPartner,
        )
    }
}

@Composable
private fun PersonCard(
    name: String,
    gender: Gender,
    birth: Birth,
    pillars: ImmutableList<SajuPillarDetail>,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(16.dp), color = TodakunColor.coolGray50)
                .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.compatibility_person_name_format, name, gender.displayName()),
                    style = TodakunTypography.body2SemiBold,
                    color = TodakunColor.gray975,
                )
                Text(
                    text = birth.displayText(),
                    style = TodakunTypography.body3Regular,
                    color = TodakunColor.gray500,
                )
            }
            Text(
                text = stringResource(R.string.compatibility_change_button),
                style = TodakunTypography.body3SemiBold,
                color = TodakunColor.primary600,
                modifier =
                    Modifier
                        .background(shape = RoundedCornerShape(8.dp), color = TodakunColor.white)
                        .border(
                            shape = RoundedCornerShape(8.dp),
                            border =
                                BorderStroke(
                                    width = 1.dp,
                                    color = TodakunColor.primary500,
                                ),
                        ).padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = onChangeClick),
            )
        }
        if (pillars.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 22.dp), 1.dp, TodakunColor.blackOpacity05)
            CompatibilitySajuPillars(
                modifier = Modifier.fillMaxWidth(),
                pillars = pillars,
            )
        }
    }
}

@Composable
private fun AddPartnerPrompt(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TodakunColor.coolGray50)
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(TodakunColor.gray100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = DesignSystemR.drawable.ic_plus),
                contentDescription = null,
                tint = TodakunColor.gray400,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.compatibility_add_partner_prompt),
            style = TodakunTypography.body2Medium,
            color = TodakunColor.gray500,
        )
    }
}

@Composable
private fun PartnerPickerSheet(
    selectedIndex: Int,
    pickerState: PartnerPickerState,
    onDismissRequest: () -> Unit,
    onAddNewPartner: () -> Unit,
    onSelectPartner: (String) -> Unit,
) {
    val items = pickerState.partners.map { "${it.name} (${it.relationshipType.label})" }
    // 맨 위의 사용자를 선택할 떄, 바텀시트를 열었다 닫아도 선택되도록 하는 플래그
    var isOnceSelected by remember { mutableStateOf(false) }

    WheelPickerBottomSheet(onDismissRequest = {
        if (!isOnceSelected) onSelectPartner(pickerState.partners[selectedIndex].linkId)
        onDismissRequest()
    }) {
        when (pickerState.partnersState) {
            is PartnerListState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    TodakunProgressIndicator()
                }
            }

            is PartnerListState.Success, is PartnerListState.Failure -> {
                TodakunWheelPicker(
                    title = stringResource(R.string.compatibility_partner_picker_title),
                    onSaveClick = onAddNewPartner,
                    saveButtonLabel = stringResource(R.string.compatibility_partner_picker_add),
                    columns = listOf(WheelPickerColumnState(items = items, selectedIndex = selectedIndex)),
                    onWheelPickerColumnSelect = { _, selectedIndex ->
                        onSelectPartner(pickerState.partners[selectedIndex].linkId)
                        isOnceSelected = true
                    },
                    directInputEnabled = false,
                )
            }
        }
    }
}

@Composable
private fun Gender.displayName(): String =
    when (this) {
        Gender.MALE -> stringResource(DesignSystemR.string.gender_male)
        Gender.FEMALE -> stringResource(DesignSystemR.string.gender_female)
    }

@Composable
private fun DateType.displayName(): String =
    when (this) {
        DateType.SOLAR -> stringResource(DesignSystemR.string.date_type_solar)
        DateType.LUNAR -> stringResource(DesignSystemR.string.date_type_lunar)
    }

@Composable
private fun Birth.displayText(): String {
    val dateText = date.format(BirthDateFormatter)
    val dateTypeText = dateType.displayName()

    val start = time.startTime
    val end = time.endTime
    if (time == BirthTime.UNKNOWN || start == null || end == null) {
        return stringResource(R.string.compatibility_birth_date_only_format, dateText, dateTypeText)
    }

    return stringResource(
        R.string.compatibility_birth_format,
        dateText,
        dateTypeText,
        String.format(Locale.US, "%02d:%02d", start.hour, start.minute),
        String.format(Locale.US, "%02d:%02d", end.minusMinutes(1).hour, end.minusMinutes(1).minute),
        time.displayName,
    )
}

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

private object CompatibilityEntryDefaults {
    val ButtonAreaHeight = 80.dp
}

@Preview(showBackground = true)
@Composable
private fun CompatibilityInputScreenPreview() {
    TodakunTheme {
        CompatibilityInputScreen(
            state =
                CompatibilityInputState(
                    myInfoState = MyInfoLoadState.Success,
                    myUser =
                        User(
                            id = "me",
                            name = "토닥이",
                            gender = Gender.FEMALE,
                            job = Job.WORKER,
                            relationshipStatus = RelationshipStatus.SOLO,
                            birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
                        ),
                    selectedPartner =
                        PartnerSaju(
                            linkId = "partner-1",
                            name = "토실이",
                            gender = Gender.MALE,
                            relationshipType =
                                com.kikidan.domain.model.saju
                                    .RelationshipType("LOVER", "연인"),
                            birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
                        ),
                ),
            onBackClick = {},
            onOpenPartnerPicker = {},
            onDismissPartnerPicker = {},
            onAddNewPartner = {},
            onSelectPartner = {},
            onCheckCompatibilityClick = {},
            onChangeMyInfo = {},
        )
    }
}
