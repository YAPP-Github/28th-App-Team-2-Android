package com.kikidan.mypage.mansaeryeok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadge
import com.kikidan.designsystem.component.TodakunBadgeType
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.mypage.mansaeryeok.model.FiveElementDistribution
import com.kikidan.mypage.mansaeryeok.model.FiveElementStatus

@Composable
internal fun FiveElementCard(
    fiveElements: List<FiveElementDistribution>,
    modifier: Modifier = Modifier,
) {
    val totalCount = fiveElements.sumOf { it.count }.coerceAtLeast(1)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.white)
                .border(1.dp, TodakunColor.gray100, RoundedCornerShape(16.dp))
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        fiveElements.forEach { element ->
            FiveElementRow(element = element, totalCount = totalCount)
        }
    }
}

@Composable
private fun FiveElementRow(
    element: FiveElementDistribution,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                stringResource(
                    R.string.mansaeryeok_ohaeng_label_format,
                    element.ohaeng.displayName,
                    element.ohaeng.hanja,
                ),
            style = TodakunTypography.body2Medium,
            color = TodakunColor.black,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TodakunBadge(text = element.count.toString(), type = TodakunBadgeType.Gray)

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(TodakunColor.gray50),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(element.count.toFloat() / totalCount)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(element.ohaeng.progressColor()),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        TodakunBadge(text = stringResource(element.status.labelRes), type = element.status.badgeType)
    }
}

private fun Ohaeng.progressColor(): Color =
    when (this) {
        Ohaeng.MOK -> TodakunColor.teal300
        Ohaeng.HWA -> TodakunColor.red300
        Ohaeng.TO -> TodakunColor.orange300
        Ohaeng.GEUM -> TodakunColor.coolGray400
        Ohaeng.SU -> TodakunColor.sky300
    }

@Preview(showBackground = true)
@Composable
private fun FiveElementCardPreview() {
    TodakunTheme {
        FiveElementCard(
            fiveElements =
                listOf(
                    FiveElementDistribution(Ohaeng.MOK, count = 1, status = FiveElementStatus.LACKING),
                    FiveElementDistribution(Ohaeng.HWA, count = 2, status = FiveElementStatus.MODERATE),
                    FiveElementDistribution(Ohaeng.TO, count = 3, status = FiveElementStatus.ABUNDANT),
                    FiveElementDistribution(Ohaeng.GEUM, count = 1, status = FiveElementStatus.MODERATE),
                    FiveElementDistribution(Ohaeng.SU, count = 1, status = FiveElementStatus.LACKING),
                ),
        )
    }
}
