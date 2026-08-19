package com.kikidan.mypage.setting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunDivider
import com.kikidan.designsystem.component.TodakunDividerType
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun AppSettingScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
    onTermsOfServiceClick: () -> Unit = {},
    onWithdrawalClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.app_setting_title),
            onBackClick = onBackClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            AppSettingRow(label = stringResource(R.string.app_setting_privacy_policy), onClick = onPrivacyPolicyClick)

            Spacer(modifier = Modifier.height(8.dp))
            TodakunDivider(type = TodakunDividerType.Line)
            Spacer(modifier = Modifier.height(8.dp))

            AppSettingRow(
                label = stringResource(R.string.app_setting_terms_of_service),
                onClick = onTermsOfServiceClick,
            )

            Spacer(modifier = Modifier.height(8.dp))
            TodakunDivider(type = TodakunDividerType.Line)
            Spacer(modifier = Modifier.height(8.dp))

            AppSettingRow(label = stringResource(R.string.app_setting_withdrawal), onClick = onWithdrawalClick)
        }
    }
}

@Composable
private fun AppSettingRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TodakunTypography.body2Medium,
            color = TodakunColor.black,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_small_right),
            contentDescription = null,
            tint = TodakunColor.gray400,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSettingScreenPreview() {
    TodakunTheme {
        AppSettingScreen()
    }
}
