package com.kikidan.sajucontents.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunChip2
import com.kikidan.designsystem.component.TodakunTooltip
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.model.compatibility.CompatibilityOhaeng
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.model.saju.SajuPillarDetail
import com.kikidan.sajucontents.BuildConfig
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.component.CompatibilityOhaengBars
import com.kikidan.sajucontents.component.CompatibilityScoreCard
import com.kikidan.sajucontents.component.FortuneShareDialog
import com.kikidan.sajucontents.component.SajuCard
import com.kikidan.sajucontents.model.CompatibilityResultState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.kikidan.designsystem.R as DesignSystemR

private fun shareUrlFor(id: String): String = "${BuildConfig.APP_LINK_HOST}/compatibility?id=$id"

@Composable
internal fun CompatibilityResultScreen(
    state: CompatibilityResultState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onShareDismiss: () -> Unit,
    onKakaoShareFail: () -> Unit,
    onUrlCopy: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF00010B)),
    ) {
        Image(
            painter = painterResource(id = DesignSystemR.drawable.img_result_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
                    .hazeSource(state = hazeState),
        )

        CompatibilityResultContent(
            state = state,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onAskTodakClick = onAskTodakClick,
            hazeState = hazeState,
        )

        if (state is CompatibilityResultState.Success && state.isShareDialogVisible) {
            val result = state.compatibilityResult
            FortuneShareDialog(
                fortuneTitle = result.headline,
                fortuneId = result.id,
                shareUrl = shareUrlFor(result.id),
                onKakaoShareFail = onKakaoShareFail,
                onUrlCopy = onUrlCopy,
                onDismiss = onShareDismiss,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.CompatibilityResultContent(
    state: CompatibilityResultState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onAskTodakClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val success = state as? CompatibilityResultState.Success

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .systemBarsPadding(),
    ) {
        ResultHeader(
            title = stringResource(id = R.string.compatibility_result_header_title),
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            shareEnabled = success != null,
        )
        if (success != null) {
            val result = success.compatibilityResult
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TitleBanner(result = result)
                CompatibilityScoreCard(
                    score = result.score,
                    subheadline = result.subheadline,
                    summary = result.summary,
                    hazeState = hazeState,
                )
                if (success.myPillars.isNotEmpty() && success.partnerPillars.isNotEmpty()) {
                    SajuStructureSection(
                        partnerName = result.partnerName,
                        myPillars = success.myPillars,
                        partnerPillars = success.partnerPillars,
                    )
                }
                OhaengSection(ohaengs = result.ohaengs)
                TotalAnalysisSection(totalAnalysis = result.totalAnalysis, analysisBasis = result.analysisBasis)
            }
        }
    }

    if (success != null) {
        val tooltipState = rememberTooltipState(isPersistent = true)
        LaunchedEffect(tooltipState) {
            tooltipState.show()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            TodakunTooltip(
                text = stringResource(id = R.string.compatibility_ask_todak_tooltip),
                state = tooltipState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                PrimaryButton(
                    text = stringResource(id = R.string.compatibility_ask_todak),
                    onClick = onAskTodakClick,
                    size = TodakunButtonSize.Large,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ResultHeader(
    title: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    shareEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
    ) {
        Icon(
            painter = painterResource(id = DesignSystemR.drawable.ic_chevron_left),
            contentDescription = stringResource(id = DesignSystemR.string.header_back_content_description),
            tint = TodakunColor.white,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    .size(20.dp)
                    .clickable(onClick = onBackClick),
        )
        Text(
            text = title,
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.white,
            modifier = Modifier.align(Alignment.Center),
        )
//        Icon(
//            painter = painterResource(id = DesignSystemR.drawable.ic_share),
//            contentDescription = stringResource(id = R.string.compatibility_share),
//            tint = if (shareEnabled) TodakunColor.white else TodakunColor.whiteOpacity60,
//            modifier =
//                Modifier
//                    .align(Alignment.CenterEnd)
//                    .size(20.dp)
//                    .clickable(enabled = shareEnabled, onClick = onShareClick),
//        )
    }
}

@Composable
private fun TitleBanner(result: Compatibility) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        ) {
            TodakunChip2(text = result.relationshipType.label)
            Text(
                text = stringResource(id = R.string.compatibility_partner_and_me_format, result.partnerName),
                style = TodakunTypography.body2Medium,
                color = TodakunColor.primary300,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = result.headline,
            style = TodakunTypography.heading3Bold,
            color = TodakunColor.white,
        )
    }
}

@Composable
private fun SajuStructureSection(
    partnerName: String,
    myPillars: ImmutableList<SajuPillarDetail>,
    partnerPillars: ImmutableList<SajuPillarDetail>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity10)
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.compatibility_saju_structure_title),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.white,
        )
        PersonPillarRow(label = partnerName, myPillars = myPillars, partnerPillars)
    }
}

@Composable
private fun PersonPillarRow(
    label: String,
    myPillars: ImmutableList<SajuPillarDetail>,
    partnerPillars: ImmutableList<SajuPillarDetail>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PillarTableRow(labelText = null) {
            sajuLabelString.forEach { labelRes ->
                Text(
                    text = stringResource(labelRes),
                    style = TodakunTypography.caption3Regular,
                    color = TodakunColor.whiteOpacity90,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        PillarTableRow(labelText = stringResource(id = R.string.compatibility_saju_structure_me)) {
            PillarCells(pillars = myPillars)
        }
        PillarTableRow(labelText = label) {
            PillarCells(pillars = partnerPillars)
        }
    }
}

@Composable
private fun RowScope.PillarCells(pillars: ImmutableList<SajuPillarDetail>) {
    (0..3).forEach { i ->
        if (pillars.size > i) {
            SajuCard(
                hanja = pillars[i].cheonGan.hanja,
                reading = pillars[i].cheonGan.displayName,
                ohaeng = pillars[i].cheonGan.ohaeng,
                modifier = Modifier.weight(1f),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PillarTableRow(
    labelText: String?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = labelText.orEmpty(),
            style = TodakunTypography.body1Medium,
            color = TodakunColor.white,
            modifier = Modifier.width(PillarTableLabelWidth),
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            content()
        }
    }
}

private val PillarTableLabelWidth = 44.dp

private val sajuLabelString =
    persistentListOf(
        R.string.compatibility_saju_time_label,
        R.string.compatibility_saju_day_label,
        R.string.compatibility_saju_month_label,
        R.string.compatibility_saju_year_label,
    )

@Composable
private fun OhaengSection(
    ohaengs: List<CompatibilityOhaeng>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity10)
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.compatibility_ohaeng_title),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.white,
        )
        CompatibilityOhaengBars(ohaengs = ohaengs)
    }
}

@Composable
private fun TotalAnalysisSection(
    totalAnalysis: String,
    analysisBasis: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity10)
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.compatibility_total_analysis_title),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.white,
        )
        Text(text = totalAnalysis, style = TodakunTypography.body2Regular, color = TodakunColor.white)
        HorizontalDivider(thickness = 1.dp, color = TodakunColor.whiteOpacity10)
        Text(
            text = stringResource(id = R.string.compatibility_analysis_basis_format, analysisBasis),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.whiteOpacity60,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompatibilityResultScreenPreview() {
    val sample =
        Compatibility(
            id = "preview-id",
            partnerName = "토실이",
            relationshipType = RelationshipType("LOVER", "연인"),
            score = 85,
            headline = "함께할 수록 빛나는 궁합",
            subheadline = "함께 있을 때, 편안함이 커지는 사이예요.",
            summary = "두 분은 서로의 부족한 기운을 보완하며 평온한 안식처가 되어주는 최상의 흐름을 가지고 있습니다.",
            totalAnalysis = "두 분의 사주는 서로를 보완하는 좋은 흐름을 보이고 있어요.",
            analysisBasis = "사주 팔자 기반",
            ohaengs =
                listOf(
                    CompatibilityOhaeng(Ohaeng.MOK, 25),
                    CompatibilityOhaeng(Ohaeng.HWA, 30),
                    CompatibilityOhaeng(Ohaeng.TO, 15),
                    CompatibilityOhaeng(Ohaeng.GEUM, 15),
                    CompatibilityOhaeng(Ohaeng.SU, 15),
                ),
        )
    TodakunTheme {
        CompatibilityResultScreen(
            state = CompatibilityResultState.Success(compatibilityResult = sample),
            onBackClick = {},
            onShareClick = {},
            onShareDismiss = {},
            onKakaoShareFail = {},
            onUrlCopy = {},
            onAskTodakClick = {},
        )
    }
}
