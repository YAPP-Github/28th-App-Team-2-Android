package com.kikidan.mypage.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunToggle
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.component.wheelpicker.TimeWheelPicker
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.mypage.notification.model.NotificationSettingUiModel
import com.kikidan.mypage.notification.model.NotificationSettingUiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationSettingScreen(
    uiState: NotificationSettingUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onMorningReportToggle: () -> Unit = {},
    onMorningReportTimeChange: (LocalTime) -> Unit = {},
    onTodakiToggle: () -> Unit = {},
    onLuckyActionReminderToggle: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.notification_setting_title),
            onBackClick = onBackClick,
        )

        when (uiState) {
            is NotificationSettingUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
            }

            is NotificationSettingUiState.Fail -> {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
            }

            is NotificationSettingUiState.Success -> {
                NotificationSettingContent(
                    model = uiState.model,
                    onMorningReportToggle = onMorningReportToggle,
                    onMorningReportTimeChange = onMorningReportTimeChange,
                    onTodakiToggle = onTodakiToggle,
                    onLuckyActionReminderToggle = onLuckyActionReminderToggle,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingContent(
    model: NotificationSettingUiModel,
    onMorningReportToggle: () -> Unit,
    onMorningReportTimeChange: (LocalTime) -> Unit,
    onTodakiToggle: () -> Unit,
    onLuckyActionReminderToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotificationSettingCard(
            title = stringResource(R.string.notification_setting_morning_report_title),
            subtitle = stringResource(R.string.notification_setting_morning_report_subtitle),
            checked = model.morningReportEnabled,
            onCheckedChange = { onMorningReportToggle() },
            extraContent = {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = { showTimePicker = true }),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.notification_setting_morning_report_time_label),
                        style = TodakunTypography.body3Medium,
                        color = TodakunColor.gray500,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = MorningReportTimeFormatter.format(model.morningReportTime),
                            style = TodakunTypography.body3Medium,
                            color = TodakunColor.gray925,
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            tint = TodakunColor.gray400,
                        )
                    }
                }
            },
        )

        NotificationSettingCard(
            title = stringResource(R.string.notification_setting_todak_alarm_title),
            subtitle = stringResource(R.string.notification_setting_todak_alarm_subtitle),
            checked = model.todakiEnabled,
            onCheckedChange = { onTodakiToggle() },
        )

        NotificationSettingCard(
            title = stringResource(R.string.notification_setting_lucky_action_title),
            subtitle = stringResource(R.string.notification_setting_lucky_action_subtitle),
            checked = model.luckyActionReminderEnabled,
            onCheckedChange = { onLuckyActionReminderToggle() },
        )
    }

    if (showTimePicker) {
        var selectedHour by remember(model.morningReportTime) { mutableIntStateOf(model.morningReportTime.hour) }
        var selectedMinute by remember(model.morningReportTime) { mutableIntStateOf(model.morningReportTime.minute) }
        TimeWheelPicker(
            hour = model.morningReportTime.hour,
            minute = model.morningReportTime.minute,
            minuteStep = MORNING_REPORT_MINUTE_STEP,
            onHourChange = { selectedHour = it },
            onMinuteChange = { selectedMinute = it },
            onSaveClick = {
                onMorningReportTimeChange(LocalTime.of(selectedHour, selectedMinute))
                showTimePicker = false
            },
            onDismissRequest = { showTimePicker = false },
        )
    }
}

@Composable
private fun NotificationSettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, TodakunColor.gray200, RoundedCornerShape(16.dp))
                .background(TodakunColor.white)
                .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = title, style = TodakunTypography.body2Medium, color = TodakunColor.black)
                Text(text = subtitle, style = TodakunTypography.body3Medium, color = TodakunColor.gray500)
            }
            TodakunToggle(checked = checked, onCheckedChange = onCheckedChange)
        }
        extraContent()
    }
}

private val MorningReportTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
private const val MORNING_REPORT_MINUTE_STEP = 30

@Preview(showBackground = true)
@Composable
private fun NotificationSettingScreenPreview() {
    TodakunTheme {
        NotificationSettingScreen(
            uiState =
                NotificationSettingUiState.Success(
                    NotificationSettingUiModel(
                        morningReportEnabled = true,
                        morningReportTime = LocalTime.of(8, 0),
                        todakiEnabled = true,
                        luckyActionReminderEnabled = true,
                    ),
                ),
        )
    }
}
