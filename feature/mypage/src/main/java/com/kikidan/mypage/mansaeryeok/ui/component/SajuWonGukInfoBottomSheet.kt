package com.kikidan.mypage.mansaeryeok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SajuWonGukInfoBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TodakunColor.white,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.mansaeryeok_saju_won_guk_info_title),
                    style = TodakunTypography.heading4Bold,
                    color = TodakunColor.gray975,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(R.string.header_close_content_description),
                    tint = TodakunColor.gray500,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clickable(onClick = onDismissRequest),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.mansaeryeok_saju_won_guk_info_description),
                style = TodakunTypography.body2Medium,
                color = TodakunColor.gray925,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TodakunColor.gray25)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.mansaeryeok_saju_won_guk_info_year),
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.gray975,
                )
                Text(
                    text = stringResource(R.string.mansaeryeok_saju_won_guk_info_month),
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.gray975,
                )
                Text(
                    text = stringResource(R.string.mansaeryeok_saju_won_guk_info_day),
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.gray975,
                )
                Text(
                    text = stringResource(R.string.mansaeryeok_saju_won_guk_info_hour),
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.gray975,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            PrimaryButton(
                text = stringResource(R.string.mansaeryeok_info_confirm_button),
                onClick = onDismissRequest,
                size = TodakunButtonSize.Large,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SajuWonGukInfoBottomSheetPreview() {
    TodakunTheme {
        SajuWonGukInfoBottomSheet(onDismissRequest = {})
    }
}
