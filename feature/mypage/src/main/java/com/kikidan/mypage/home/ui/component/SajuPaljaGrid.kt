package com.kikidan.mypage.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar

@Composable
internal fun SajuPaljaGrid(
    sajuPalja: SajuPalja,
    modifier: Modifier = Modifier,
) {
    val pillars: List<SajuPillar> =
        listOfNotNull(sajuPalja.yearPillar, sajuPalja.monthPillar, sajuPalja.dayPillar, sajuPalja.hourPillar)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.coolGray50)
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pillars.forEach { pillar ->
                SajuCard(
                    hanja = pillar.cheonGan.hanja,
                    reading = pillar.cheonGan.displayName,
                    ohaeng = pillar.cheonGan.ohaeng,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pillars.forEach { pillar ->
                SajuCard(
                    hanja = pillar.jiJi.hanja,
                    reading = pillar.jiJi.displayName,
                    ohaeng = pillar.jiJi.ohaeng,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SajuPaljaGridPreview() {
    TodakunTheme {
        SajuPaljaGrid(
            sajuPalja =
                SajuPalja(
                    yearPillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                    monthPillar = SajuPillar(CheonGan.GI, JiJi.SA),
                    dayPillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                    hourPillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                ),
        )
    }
}
