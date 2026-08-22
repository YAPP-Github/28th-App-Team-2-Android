package com.kikidan.mypage.setting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunCheckbox
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.dialog.TodakunDialog
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun AppSettingWithdrawalNoticeScreen(
    modifier: Modifier = Modifier,
    isWithdrawing: Boolean = false,
    onBackClick: () -> Unit = {},
    onWithdrawConfirm: () -> Unit = {},
) {
    var agreed by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.app_setting_withdrawal_notice_title),
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

            Text(
                text = stringResource(R.string.app_setting_withdrawal_notice_body),
                style = TodakunTypography.body2Medium,
                color = TodakunColor.gray950,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TodakunColor.gray25)
                        .padding(20.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { agreed = !agreed },
                    ),
            ) {
                TodakunCheckbox(checked = agreed, onCheckedChange = { agreed = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_setting_withdrawal_agree_checkbox),
                    style = TodakunTypography.body2Medium,
                    color = TodakunColor.black,
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.app_setting_withdrawal_confirm_button),
            onClick = { showConfirmDialog = true },
            size = TodakunButtonSize.Large,
            enabled = agreed && !isWithdrawing,
            isLoading = isWithdrawing,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showConfirmDialog) {
        TodakunDialog(
            title = stringResource(R.string.app_setting_withdrawal_confirm_dialog_title),
            description = stringResource(R.string.app_setting_withdrawal_confirm_dialog_description),
            confirmText = stringResource(R.string.common_confirm),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                showConfirmDialog = false
                onWithdrawConfirm()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSettingWithdrawalNoticeScreenPreview() {
    TodakunTheme {
        AppSettingWithdrawalNoticeScreen()
    }
}
