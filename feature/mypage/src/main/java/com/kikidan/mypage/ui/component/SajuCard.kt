package com.kikidan.mypage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.Ohaeng

@Composable
internal fun SajuCard(
    hanja: String,
    reading: String,
    ohaeng: Ohaeng,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(ohaeng.containerColor()),
        contentAlignment = Alignment.Center,
    ) {
        val hanjaFontSize = (maxWidth.value * HANJA_FONT_SIZE_RATIO).sp
        val hanjaLineHeight = hanjaFontSize * HANJA_LINE_HEIGHT_RATIO
        val readingFontSize = (maxWidth.value * READING_FONT_SIZE_RATIO).sp
        val readingLineHeight = readingFontSize * READING_LINE_HEIGHT_RATIO

        val density = LocalDensity.current
        val availableHeight = maxWidth - VerticalPadding * 2
        val contentHeight = with(density) { hanjaLineHeight.toDp() + readingLineHeight.toDp() }
        val lineGap = minOf(0.dp, availableHeight - contentHeight)

        Column(
            modifier = Modifier.padding(horizontal = HorizontalPadding, vertical = VerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(lineGap),
        ) {
            Text(
                text = hanja,
                style =
                    TodakunTypography.body1Bold.copy(
                        fontSize = hanjaFontSize,
                        lineHeight = hanjaLineHeight,
                    ),
                color = TodakunColor.gray975,
            )
            Text(
                text = stringResource(R.string.saju_card_label_format, reading, ohaeng.hanja),
                style =
                    TodakunTypography.caption3Regular.copy(
                        fontSize = readingFontSize,
                        lineHeight = readingLineHeight,
                    ),
                color = TodakunColor.gray975,
            )
        }
    }
}

private fun Ohaeng.containerColor(): Color =
    when (this) {
        Ohaeng.MOK -> TodakunColor.teal200
        Ohaeng.HWA -> TodakunColor.red200
        Ohaeng.TO -> TodakunColor.orange200
        Ohaeng.GEUM -> TodakunColor.coolGray300
        Ohaeng.SU -> TodakunColor.sky200
    }

private val HorizontalPadding = 6.dp
private val VerticalPadding = 6.dp

// Figma 실측 기준(48dp 카드 → hanja 18sp/lineHeight 26, reading 10sp/lineHeight 13)에서의 비율.
// 카드 폭이 달라져도 이 비율대로 두 글자 크기가 같이 커지고 작아진다.
private const val HANJA_FONT_SIZE_RATIO = 18f / 48f
private const val READING_FONT_SIZE_RATIO = 10f / 48f
private const val HANJA_LINE_HEIGHT_RATIO = 26f / 18f
private const val READING_LINE_HEIGHT_RATIO = 13f / 10f

@Preview(showBackground = true)
@Composable
private fun SajuCardPreview() {
    TodakunTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SajuCard(hanja = "辛", reading = "신", ohaeng = Ohaeng.GEUM, modifier = Modifier.width(72.dp))
            SajuCard(hanja = "己", reading = "기", ohaeng = Ohaeng.TO, modifier = Modifier.width(72.dp))
            SajuCard(hanja = "癸", reading = "계", ohaeng = Ohaeng.SU, modifier = Modifier.width(72.dp))
            SajuCard(hanja = "丁", reading = "정", ohaeng = Ohaeng.HWA, modifier = Modifier.width(72.dp))
            SajuCard(hanja = "卯", reading = "묘", ohaeng = Ohaeng.MOK, modifier = Modifier.width(72.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SajuCardNarrowPreview() {
    TodakunTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SajuCard(hanja = "戊", reading = "무", ohaeng = Ohaeng.TO, modifier = Modifier.width(48.dp))
            SajuCard(hanja = "丙", reading = "병", ohaeng = Ohaeng.HWA, modifier = Modifier.width(48.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SajuCardScalingPreview() {
    TodakunTheme {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(32.dp, 40.dp, 48.dp, 64.dp, 90.dp, 120.dp).forEach { width ->
                SajuCard(hanja = "丙", reading = "병", ohaeng = Ohaeng.HWA, modifier = Modifier.width(width))
            }
        }
    }
}
