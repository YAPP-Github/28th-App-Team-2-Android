package com.kikidan.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography

private data class SajuItem(
    val iconRes: Int,
    val labelRes: Int,
    val subtitleRes: Int,
)

private val SajuItems =
    listOf(
        SajuItem(
            R.drawable.img_home_saju_compatibility,
            R.string.home_saju_compatibility_title,
            R.string.home_saju_compatibility_subtitle,
        ),
        SajuItem(
            R.drawable.img_home_saju_date_fortune,
            R.string.home_saju_date_selection_title,
            R.string.home_saju_date_selection_subtitle,
        ),
        SajuItem(
            R.drawable.img_home_saju_yearly_fortune,
            R.string.home_saju_yearly_fortune_title,
            R.string.home_saju_yearly_fortune_subtitle,
        ),
    )

@Composable
internal fun SajuContents(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.home_section_saju),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SajuItems.forEach { item -> SajuCard(item = item) }
        }
    }
}

@Composable
private fun SajuCard(
    item: SajuItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.white)
                .border(BorderStroke(1.dp, TodakunColor.coolGray100), RoundedCornerShape(16.dp))
                .clickable {
                    // TODO(#38): feature:saju-contents 머지 후 연결
                }.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(TodakunColor.coolGray50),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(item.labelRes),
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.gray975,
            )
            Text(
                text = stringResource(item.subtitleRes),
                style = TodakunTypography.body3Regular,
                color = TodakunColor.gray500,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SajuContentsPreview() {
    TodakunTheme { SajuContents() }
}
