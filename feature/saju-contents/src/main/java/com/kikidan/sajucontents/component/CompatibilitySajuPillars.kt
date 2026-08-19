package com.kikidan.sajucontents.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.domain.model.saju.SajuPillarType
import com.kikidan.domain.model.saju.TenGod
import com.kikidan.domain.model.saju.TwelveUnseong
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CompatibilitySajuPillars(
    pillars: ImmutableList<SajuPillarDetail>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        pillars.forEach { pillar -> PillarColumn(pillar = pillar) }
    }
}

@Composable
private fun PillarColumn(
    pillar: SajuPillarDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(54.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(pillar.pillarType.labelRes()),
            style = TodakunTypography.caption3Regular,
            color = TodakunColor.gray700,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(pillar.stemTenGod.labelRes()),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.gray975,
        )
        Spacer(modifier = Modifier.height(10.dp))

        SajuCard(
            hanja = pillar.cheonGan.hanja,
            reading = pillar.cheonGan.displayName,
            ohaeng = pillar.cheonGan.ohaeng,
        )
        Spacer(modifier = Modifier.height(8.dp))

        SajuCard(
            hanja = pillar.jiJi.hanja,
            reading = pillar.jiJi.displayName,
            ohaeng = pillar.jiJi.ohaeng,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(pillar.branchTenGod.labelRes()),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.gray975,
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(pillar.twelveUnseong.labelRes()),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.gray975,
        )
    }
}

@StringRes
private fun SajuPillarType.labelRes(): Int =
    when (this) {
        SajuPillarType.HOUR -> R.string.mansaeryeok_pillar_hour
        SajuPillarType.DAY -> R.string.mansaeryeok_pillar_day
        SajuPillarType.MONTH -> R.string.mansaeryeok_pillar_month
        SajuPillarType.YEAR -> R.string.mansaeryeok_pillar_year
    }

@StringRes
private fun TwelveUnseong.labelRes(): Int =
    when (this) {
        TwelveUnseong.JANGSAENG -> R.string.mansaeryeok_twelve_unseong_jangsaeng
        TwelveUnseong.MOKYOK -> R.string.mansaeryeok_twelve_unseong_mogyok
        TwelveUnseong.GWANDAE -> R.string.mansaeryeok_twelve_unseong_gwandae
        TwelveUnseong.GEONROK -> R.string.mansaeryeok_twelve_unseong_geonrok
        TwelveUnseong.JEWANG -> R.string.mansaeryeok_twelve_unseong_jewang
        TwelveUnseong.SOE -> R.string.mansaeryeok_twelve_unseong_soe
        TwelveUnseong.BYEONG -> R.string.mansaeryeok_twelve_unseong_byeong
        TwelveUnseong.SA -> R.string.mansaeryeok_twelve_unseong_sa
        TwelveUnseong.MYO -> R.string.mansaeryeok_twelve_unseong_myo
        TwelveUnseong.JEOL -> R.string.mansaeryeok_twelve_unseong_jeol
        TwelveUnseong.TAE -> R.string.mansaeryeok_twelve_unseong_tae
        TwelveUnseong.YANG -> R.string.mansaeryeok_twelve_unseong_yang
    }

@StringRes
private fun TenGod.labelRes(): Int =
    when (this) {
        TenGod.ILWON -> R.string.mansaeryeok_ten_god_ilwon
        TenGod.BIGYEON -> R.string.mansaeryeok_ten_god_bigyeon
        TenGod.GEOPJAE -> R.string.mansaeryeok_ten_god_gyeopjae
        TenGod.SIKSIN -> R.string.mansaeryeok_ten_god_siksin
        TenGod.SANGGWAN -> R.string.mansaeryeok_ten_god_sanggwan
        TenGod.PYEONJAE -> R.string.mansaeryeok_ten_god_pyeonjae
        TenGod.JEONGJAE -> R.string.mansaeryeok_ten_god_jeongjae
        TenGod.PYEONGWAN -> R.string.mansaeryeok_ten_god_pyeongwan
        TenGod.JEONGGWAN -> R.string.mansaeryeok_ten_god_jeonggwan
        TenGod.PYEONIN -> R.string.mansaeryeok_ten_god_pyeonin
        TenGod.JEONGIN -> R.string.mansaeryeok_ten_god_jeongin
    }

@Preview(showBackground = true)
@Composable
private fun CompatibilitySajuPillarsPreview() {
    TodakunTheme {
        CompatibilitySajuPillars(
            pillars =
                persistentListOf(
                    SajuPillarDetail(
                        SajuPillarType.HOUR,
                        CheonGan.SIN,
                        JiJi.MI,
                        TenGod.SIKSIN,
                        TenGod.BIGYEON,
                        emptyList(),
                        TwelveUnseong.GWANDAE,
                        com.kikidan.domain.model.saju.TwelveSinsal.entries
                            .first(),
                    ),
                    SajuPillarDetail(
                        SajuPillarType.DAY,
                        CheonGan.GI,
                        JiJi.SA,
                        TenGod.ILWON,
                        TenGod.BIGYEON,
                        emptyList(),
                        TwelveUnseong.GWANDAE,
                        com.kikidan.domain.model.saju.TwelveSinsal.entries
                            .first(),
                    ),
                    SajuPillarDetail(
                        SajuPillarType.MONTH,
                        CheonGan.GYE,
                        JiJi.MYO,
                        TenGod.PYEONJAE,
                        TenGod.BIGYEON,
                        emptyList(),
                        TwelveUnseong.GWANDAE,
                        com.kikidan.domain.model.saju.TwelveSinsal.entries
                            .first(),
                    ),
                    SajuPillarDetail(
                        SajuPillarType.YEAR,
                        CheonGan.JEONG,
                        JiJi.CHUK,
                        TenGod.PYEONIN,
                        TenGod.BIGYEON,
                        emptyList(),
                        TwelveUnseong.GWANDAE,
                        com.kikidan.domain.model.saju.TwelveSinsal.entries
                            .first(),
                    ),
                ),
        )
    }
}
