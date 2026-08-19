package com.kikidan.mypage.setting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunSelectField
import com.kikidan.designsystem.component.TodakunTextField
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.component.wheelpicker.WithdrawalReasonWheelPicker
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun AppSettingWithdrawalScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNextClick: (reason: String, detailReason: String) -> Unit = { _, _ -> },
) {
    var showReasonPicker by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var detailReason by remember { mutableStateOf("") }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.app_setting_withdrawal),
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.app_setting_withdrawal_question),
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.gray975,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_setting_withdrawal_description),
                style = TodakunTypography.body3Regular,
                color = TodakunColor.gray700,
            )

            Spacer(modifier = Modifier.height(32.dp))

            TodakunSelectField(
                value = reason,
                placeholder = stringResource(R.string.app_setting_withdrawal_reason_placeholder),
                onClick = { showReasonPicker = true },
                onClear = { reason = "" },
            )

            if (reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                TodakunTextField(
                    value = detailReason,
                    onValueChange = { detailReason = it.take(DETAIL_REASON_MAX_LENGTH) },
                    placeholder = stringResource(R.string.app_setting_withdrawal_detail_placeholder),
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.app_setting_withdrawal_next_button),
            onClick = { onNextClick(reason, detailReason) },
            size = TodakunButtonSize.Large,
            enabled = reason.isNotEmpty(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showReasonPicker) {
        WithdrawalReasonWheelPicker(
            onWithdrawalReasonChange = { reason = it },
            onSaveClick = { showReasonPicker = false },
            onDismissRequest = { showReasonPicker = false },
        )
    }
}

private const val DETAIL_REASON_MAX_LENGTH = 200

@Preview(showBackground = true)
@Composable
private fun AppSettingWithdrawalScreenPreview() {
    TodakunTheme {
        AppSettingWithdrawalScreen()
    }
}
