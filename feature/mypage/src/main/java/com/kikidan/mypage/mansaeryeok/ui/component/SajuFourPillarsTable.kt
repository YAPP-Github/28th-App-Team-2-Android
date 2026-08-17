package com.kikidan.mypage.mansaeryeok.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.mypage.mansaeryeok.model.SajuPillarDetail
import com.kikidan.mypage.mansaeryeok.model.TenGod
import com.kikidan.mypage.mansaeryeok.model.TwelveSinsal
import com.kikidan.mypage.mansaeryeok.model.TwelveUnseong
import com.kikidan.mypage.ui.component.SajuCard

@Composable
internal fun SajuFourPillarsTable(
    pillars: List<SajuPillarDetail>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.white)
                .border(1.dp, TodakunColor.gray100, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        TableRow(label = null) {
            pillars.forEach { pillar -> PillarHeaderCell(pillar = pillar, modifier = Modifier.width(CellWidth)) }
        }
        Spacer(modifier = Modifier.height(12.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_ten_god)) {
            pillars.forEach { pillar -> CellText(text = stringResource(pillar.topTenGod.labelRes)) }
        }
        Spacer(modifier = Modifier.height(12.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_cheongan)) {
            pillars.forEach { pillar ->
                SajuCard(
                    hanja = pillar.pillar.cheonGan.hanja,
                    reading = pillar.pillar.cheonGan.displayName,
                    ohaeng = pillar.pillar.cheonGan.ohaeng,
                    modifier = Modifier.width(CellWidth),
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_jiji)) {
            pillars.forEach { pillar ->
                SajuCard(
                    hanja = pillar.pillar.jiJi.hanja,
                    reading = pillar.pillar.jiJi.displayName,
                    ohaeng = pillar.pillar.jiJi.ohaeng,
                    modifier = Modifier.width(CellWidth),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_ten_god)) {
            pillars.forEach { pillar -> CellText(text = stringResource(pillar.bottomTenGod.labelRes)) }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_hidden_stem)) {
            pillars.forEach { pillar -> CellText(text = pillar.hiddenStem) }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_twelve_unseong)) {
            pillars.forEach { pillar -> CellText(text = stringResource(pillar.twelveUnseong.labelRes)) }
        }
        Spacer(modifier = Modifier.height(8.dp))

        TableRow(label = stringResource(R.string.mansaeryeok_row_label_twelve_sinsal)) {
            pillars.forEach { pillar -> CellText(text = stringResource(pillar.twelveSinsal.labelRes)) }
        }
    }
}

@Composable
private fun TableRow(
    label: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.orEmpty(),
            style = TodakunTypography.caption3Regular,
            color = TodakunColor.gray500,
            modifier = Modifier.width(LabelWidth),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(ColumnGap)) {
            content()
        }
    }
}

@Composable
private fun PillarHeaderCell(
    pillar: SajuPillarDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(pillar.pillarLabelRes),
            style = TodakunTypography.caption3Medium,
            color = TodakunColor.gray975,
        )
        Text(
            text = stringResource(pillar.periodLabelRes),
            style = TodakunTypography.caption3Regular,
            color = TodakunColor.gray700,
        )
    }
}

@Composable
private fun CellText(text: String) {
    Text(
        text = text,
        style = TodakunTypography.caption1Regular,
        color = TodakunColor.gray975,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(CellWidth),
    )
}

private val CellWidth = 48.dp
private val LabelWidth = 56.dp
private val ColumnGap = 24.dp

@Preview(showBackground = true)
@Composable
private fun SajuFourPillarsTablePreview() {
    TodakunTheme {
        SajuFourPillarsTable(
            pillars =
                listOf(
                    SajuPillarDetail(
                        pillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                        pillarLabelRes = R.string.mansaeryeok_pillar_hour,
                        periodLabelRes = R.string.mansaeryeok_period_late,
                        topTenGod = TenGod.SIKSIN,
                        bottomTenGod = TenGod.BIGYEON,
                        hiddenStem = "을계무",
                        twelveUnseong = TwelveUnseong.YANG,
                        twelveSinsal = TwelveSinsal.HWAGAESAL,
                    ),
                    SajuPillarDetail(
                        pillar = SajuPillar(CheonGan.GI, JiJi.SA),
                        pillarLabelRes = R.string.mansaeryeok_pillar_day,
                        periodLabelRes = R.string.mansaeryeok_period_prime,
                        topTenGod = TenGod.ILWON,
                        bottomTenGod = TenGod.BIGYEON,
                        hiddenStem = "무경병",
                        twelveUnseong = TwelveUnseong.TAE,
                        twelveSinsal = TwelveSinsal.GEOPSAL,
                    ),
                    SajuPillarDetail(
                        pillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                        pillarLabelRes = R.string.mansaeryeok_pillar_month,
                        periodLabelRes = R.string.mansaeryeok_period_youth,
                        topTenGod = TenGod.PYEONJAE,
                        bottomTenGod = TenGod.BIGYEON,
                        hiddenStem = "계신기",
                        twelveUnseong = TwelveUnseong.GWANDAE,
                        twelveSinsal = TwelveSinsal.BANANSAL,
                    ),
                    SajuPillarDetail(
                        pillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                        pillarLabelRes = R.string.mansaeryeok_pillar_year,
                        periodLabelRes = R.string.mansaeryeok_period_early,
                        topTenGod = TenGod.PYEONIN,
                        bottomTenGod = TenGod.BIGYEON,
                        hiddenStem = "을계무",
                        twelveUnseong = TwelveUnseong.YANG,
                        twelveSinsal = TwelveSinsal.HWAGAESAL,
                    ),
                ),
        )
    }
}
