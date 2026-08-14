package com.kikidan.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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

private data class SajuItem(
    val labelRes: Int,
    val subtitleRes: Int,
)

private val SajuItems =
    listOf(
        SajuItem(R.string.home_saju_compatibility_title, R.string.home_saju_compatibility_subtitle),
        SajuItem(R.string.home_saju_date_selection_title, R.string.home_saju_date_selection_subtitle),
        SajuItem(R.string.home_saju_yearly_fortune_title, R.string.home_saju_yearly_fortune_subtitle),
    )

@Composable
internal fun HomeSajuSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_section_saju),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        SajuItems.forEachIndexed { index, item ->
            SajuRow(item = item)
            if (index < SajuItems.lastIndex) {
                HorizontalDivider(
                    color = TodakunColor.gray100,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun SajuRow(
    item: SajuItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    // TODO(#38): feature:saju-contents 머지 후 연결
                }.padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(item.labelRes),
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.gray975,
            )
            Text(
                text = stringResource(item.subtitleRes),
                style = TodakunTypography.caption2Regular,
                color = TodakunColor.gray500,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.home_saju_cta),
                style = TodakunTypography.caption2Medium,
                color = TodakunColor.primary500,
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_small_right),
                contentDescription = null,
                tint = TodakunColor.primary500,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeSajuSectionPreview() {
    TodakunTheme { HomeSajuSection() }
}
