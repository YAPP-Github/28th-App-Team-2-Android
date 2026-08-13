package com.kikidan.sajucontents.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunChip2
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import com.kikidan.domain.util.yearToGanji
import com.kikidan.sajucontents.BuildConfig
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.component.FortuneScoreCard
import com.kikidan.sajucontents.component.FortuneShareDialog
import com.kikidan.sajucontents.model.YearFortuneResultState
import kotlinx.collections.immutable.toImmutableList
import com.kikidan.designsystem.R as DesignSystemR

private fun shareUrlFor(id: String): String = "https://${BuildConfig.APP_LINK_HOST}/year-fortune?id=$id"

@Composable
internal fun YearFortuneResultScreen(
    state: YearFortuneResultState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onShareDismiss: () -> Unit,
    onKakaoShareFail: () -> Unit,
    onUrlCopy: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fortune = state.fortuneResult

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.black),
    ) {
        Image(
            painter = painterResource(id = DesignSystemR.drawable.img_result_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(0.5f),
        )

        YearFortuneResultContent(
            fortune = fortune,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onAskTodakClick = onAskTodakClick,
        )

        if (state.isShareDialogVisible && fortune != null) {
            FortuneShareDialog(
                fortuneTitle = fortune.title,
                fortuneId = fortune.id,
                shareUrl = shareUrlFor(fortune.id),
                onKakaoShareFail = onKakaoShareFail,
                onUrlCopy = onUrlCopy,
                onDismiss = onShareDismiss,
            )
        }
    }
}

@Composable
private fun BoxScope.YearFortuneResultContent(
    fortune: YearFortune?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .systemBarsPadding(),
    ) {
        ResultHeader(
            title = stringResource(id = R.string.year_fortune_result_header_title),
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            shareEnabled = fortune != null,
        )
        if (fortune != null) {
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
                    ganjiLabel = yearToGanji(fortune.year),
                    title = stringResource(id = R.string.year_fortune_result_title, fortune.year),
                    modifier = Modifier.fillMaxWidth(),
                )
                FortuneScoreCard(
                    score = fortune.score,
                    title = fortune.title,
                    categoryStars = fortune.categories.toImmutableList(),
                    scoreLabel = stringResource(id = R.string.year_fortune_score_label),
                )
                SummaryCard(content = fortune.content)
            }
        }
    }

    if (fortune != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            PrimaryButton(
                text = stringResource(id = R.string.year_fortune_ask_todak),
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
        Icon(
            painter = painterResource(id = DesignSystemR.drawable.ic_share),
            contentDescription = stringResource(id = R.string.year_fortune_share),
            tint = if (shareEnabled) TodakunColor.white else TodakunColor.whiteOpacity60,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(20.dp)
                    .clickable(enabled = shareEnabled, onClick = onShareClick),
        )
    }
}

@Composable
private fun ResultHeadline(
    ganjiLabel: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TodakunChip2(text = ganjiLabel)
        Text(
            text = title,
            style = TodakunTypography.heading3Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummaryCard(
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity10)
                .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(id = R.string.year_fortune_summary_title),
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
private fun YearFortuneResultScreenPreview() {
    val sample =
        YearFortune(
            id = "preview-id",
            year = 2026,
            score = 85,
            title = "새로운 도전이 결실을 맺는 해예요.",
            content = "요약 분석 예시 본문입니다.",
            categories =
                listOf(
                    FortuneCategoryStar(FortuneCategory.ACHIEVEMENT, 3),
                    FortuneCategoryStar(FortuneCategory.MONEY, 2),
                    FortuneCategoryStar(FortuneCategory.RELATIONSHIP, 1),
                ),
        )
    TodakunTheme {
        YearFortuneResultScreen(
            state = YearFortuneResultState(fortuneResult = sample),
            onBackClick = {},
            onShareClick = {},
            onShareDismiss = {},
            onKakaoShareFail = {},
            onUrlCopy = {},
            onAskTodakClick = {},
        )
    }
}
