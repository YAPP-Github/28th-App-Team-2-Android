package com.kikidan.designsystem.component.header

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

@Composable
fun TodakunMainHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    bellClickEnabled: Boolean = true,
    onBellClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(TodakunColor.white)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.black,
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    style = TodakunTypography.body3Regular,
                    color = TodakunColor.gray500,
                )
            }
        }

        if (bellClickEnabled) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bell),
                contentDescription = stringResource(R.string.header_notice_content_description),
                tint = TodakunColor.gray975,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clickable(onClick = onBellClick),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunMainHeaderPreview() {
    TodakunTheme {
        TodakunMainHeader(
            title = "타이틀",
            subtext = "서브텍스트",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodakunMainHeaderNoSubtextPreview() {
    TodakunTheme {
        TodakunMainHeader(
            title = "타이틀",
        )
    }
}
