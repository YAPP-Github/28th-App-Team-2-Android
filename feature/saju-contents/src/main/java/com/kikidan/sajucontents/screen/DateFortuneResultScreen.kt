package com.kikidan.sajucontents.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunChip2
import com.kikidan.designsystem.component.TodakunTooltip
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.sajucontents.BuildConfig
import com.kikidan.sajucontents.component.FortuneScoreCard
import com.kikidan.sajucontents.component.FortuneShareDialog
import com.kikidan.sajucontents.component.ResultDateTabRow
import com.kikidan.sajucontents.model.DateFortuneResultState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate

private fun shareUrlFor(id: String): String = "${BuildConfig.APP_LINK_HOST}/day-fortune?id=$id"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateFortuneResultScreen(
    state: DateFortuneResultState,
    onBackClick: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onShareClick: () -> Unit,
    onShareDismiss: () -> Unit,
    onKakaoShareFail: () -> Unit,
    onUrlCopy: () -> Unit,
    onExportClick: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedFortune = state.results.getOrNull(state.selectedResultIndex)
    val hazeState = rememberHazeState()

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF00010B)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_result_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
                    .hazeSource(state = hazeState),
        )

        if (selectedFortune == null) return@Box

        DateFortuneResultContent(
            onBackClick = onBackClick,
            onExportClick = onExportClick,
            onShareClick = onShareClick,
            onTabSelect = onTabSelect,
            onAskTodakClick = onAskTodakClick,
            state = state,
            selectedFortune = selectedFortune,
            hazeState = hazeState,
        )

        if (state.isShareDialogVisible) {
            FortuneShareDialog(
                fortuneTitle = selectedFortune.title,
                fortuneId = selectedFortune.id,
                shareUrl = shareUrlFor(selectedFortune.id),
                onKakaoShareFail = onKakaoShareFail,
                onUrlCopy = onUrlCopy,
                onDismiss = onShareDismiss,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.DateFortuneResultContent(
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onAskTodakClick: () -> Unit,
    state: DateFortuneResultState,
    selectedFortune: DayFortune,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    LaunchedEffect(Unit) { tooltipState.show() }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .systemBarsPadding(),
    ) {
        ResultHeader(
            title = stringResource(id = R.string.date_fortune_result_title),
            onBackClick = onBackClick,
            onExportClick = onExportClick,
            onShareClick = onShareClick,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ResultHeadline(
                purposeLabel = selectedFortune.purpose.label(),
                modifier = Modifier.fillMaxWidth(),
            )
            ResultDateTabRow(
                results = state.results,
                selectedIndex = state.selectedResultIndex,
                onTabSelect = onTabSelect,
            )
            FortuneScoreCard(
                score = selectedFortune.score,
                title = selectedFortune.title,
                categoryStars = selectedFortune.categoryStars.toImmutableList(),
                hazeState = hazeState,
            )
            SummaryCard(content = selectedFortune.content, hazeState = hazeState)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        TodakunTooltip(
            text = stringResource(id = R.string.date_fortune_ask_time_hint),
            state = tooltipState,
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.date_fortune_ask_todak),
                onClick = onAskTodakClick,
                size = TodakunButtonSize.Large,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ResultHeader(
    title: String,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit,
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
            painter = painterResource(id = R.drawable.ic_chevron_left),
            contentDescription = stringResource(id = R.string.header_back_content_description),
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
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_event_export),
                contentDescription = stringResource(id = R.string.date_fortune_export_calendar),
                tint = TodakunColor.white,
                modifier =
                    Modifier
                        .size(20.dp)
                        .clickable(onClick = onExportClick),
            )
//            Icon(
//                painter = painterResource(id = R.drawable.ic_share),
//                contentDescription = stringResource(id = R.string.date_fortune_share),
//                tint = TodakunColor.white,
//                modifier =
//                    Modifier
//                        .size(20.dp)
//                        .clickable(onClick = onShareClick),
//            )
        }
    }
}

@Composable
private fun ResultHeadline(
    purposeLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TodakunChip2(text = purposeLabel)

        val subtitle = stringResource(id = R.string.date_fortune_result_subtitle)
        val highlight = stringResource(id = R.string.date_fortune_result_highlight)
        val annotatedSubtitle =
            remember(subtitle, highlight) {
                val start = subtitle.indexOf(highlight)
                buildAnnotatedString {
                    if (start < 0) {
                        append(subtitle)
                    } else {
                        append(subtitle.substring(0, start))
                        withStyle(SpanStyle(color = TodakunColor.primary200)) {
                            append(highlight)
                        }
                        append(subtitle.substring(start + highlight.length))
                    }
                }
            }
        Text(
            text = annotatedSubtitle,
            style = TodakunTypography.heading3Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummaryCard(
    content: String,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .hazeEffect(state = hazeState) { blurRadius = 20.dp }
                .background(TodakunColor.whiteOpacity10)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.date_fortune_summary_title),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.white,
        )
        Text(
            text = content,
            style = TodakunTypography.body2Regular,
            color = TodakunColor.white,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateFortuneResultScreenPreview() {
    val sampleFortune =
        DayFortune(
            id = "preview-id",
            purpose = DayFortunePurpose.TRAVEL,
            targetDate = LocalDate.now(),
            score = 85,
            title = "이 날짜엔 새로운 시작이 아주 잘 맞아요.",
            content = "요약 분석 예시 본문입니다.",
            categoryStars =
                listOf(
                    FortuneCategoryStar(
                        category = FortuneCategory.RELATIONSHIP,
                        star = 3,
                    ),
                ),
        )
    TodakunTheme {
        DateFortuneResultScreen(
            state =
                DateFortuneResultState(
                    isLoading = false,
                    results = persistentListOf(sampleFortune),
                    selectedResultIndex = 0,
                ),
            onBackClick = {},
            onTabSelect = {},
            onShareClick = {},
            onShareDismiss = {},
            onKakaoShareFail = {},
            onUrlCopy = {},
            onExportClick = {},
            onAskTodakClick = {},
        )
    }
}
