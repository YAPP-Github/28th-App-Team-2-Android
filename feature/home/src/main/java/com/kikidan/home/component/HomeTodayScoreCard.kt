package com.kikidan.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

private val CardShape = RoundedCornerShape(16.dp)

@Composable
internal fun HomeTodayScoreCard(
    totalScore: Int,
    scoreLabel: String,
    onFortuneReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(CardShape)
                .border(1.dp, TodakunColor.whiteOpacity20, CardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.home_today_score_label),
                style = TodakunTypography.caption3Regular,
                color = TodakunColor.whiteOpacity60,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_today_score_value, totalScore),
                    style = TodakunTypography.body2SemiBold,
                    color = TodakunColor.white,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "∙ $scoreLabel",
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.primary300,
                )
            }
        }
        OutlinedButton(
            onClick = onFortuneReportClick,
            shape = RoundedCornerShape(99.dp),
            border = BorderStroke(1.dp, TodakunColor.white),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = stringResource(R.string.home_fortune_report_button),
                style = TodakunTypography.body3Medium,
                color = TodakunColor.white,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_small_right),
                contentDescription = null,
                tint = TodakunColor.white,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun HomeTodayScoreCardPreview() {
    TodakunTheme {
        HomeTodayScoreCard(
            totalScore = 72,
            scoreLabel = "흐름 좋은 날",
            onFortuneReportClick = {},
        )
    }
}
