package com.kikidan.sajucontents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.component.ShareBottomSheet
import com.kikidan.sajucontents.model.YearFortuneState
import com.kikidan.designsystem.R as DesignSystemR

// ponytail: 공유용 웹 랜딩 페이지가 아직 없어(설계 문서 4절) 연도 기반 placeholder URL을 복사한다.
// 실제 공유 링크 API/페이지가 정해지면 교체.
private fun shareUrlFor(year: Int): String = "https://todakun.com/year-fortune/$year"

@Composable
internal fun YearFortuneResultScreen(
    state: YearFortuneState,
    onBackClick: () -> Unit,
    onShareIconClick: () -> Unit,
    onShareSheetDismiss: () -> Unit,
    onAskTodakClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fortune = state.fortuneResult

    Box(modifier = modifier.fillMaxSize().background(TodakunColor.black)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 로딩/실패/프로세스 사망 복귀 상태를 구분하기 위해 헤더(뒤로가기 포함)는 항상 렌더링한다.
            YearFortuneResultHeader(
                onBackClick = onBackClick,
                onShareIconClick = onShareIconClick,
                shareEnabled = fortune != null,
            )

            when {
                fortune != null -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        TodakunChip2(
                            text = yearToGanji(fortune.year),
                            modifier = Modifier.padding(top = 16.dp),
                        )
                        Text(
                            text = stringResource(id = R.string.year_fortune_result_title, fortune.year),
                            style = TodakunTypography.heading3Bold,
                            color = TodakunColor.white,
                            modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
                        )
                        YearScoreBadge(
                            score = fortune.score,
                            headline = fortune.title,
                            categories = fortune.categories,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        SummaryCard(content = fortune.content, modifier = Modifier.padding(bottom = 24.dp))
                        PrimaryButton(
                            text = stringResource(id = R.string.year_fortune_ask_todak),
                            onClick = onAskTodakClick,
                            size = TodakunButtonSize.Large,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        )
                    }
                }

                state.error != null -> {
                    YearFortuneErrorContent(
                        message = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 80.dp),
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TodakunColor.white)
                    }
                }
            }
        }

        if (state.isShareSheetVisible && fortune != null) {
            ShareBottomSheet(
                shareUrl = shareUrlFor(fortune.year),
                onDismissRequest = onShareSheetDismiss,
            )
        }
    }
}

@Composable
private fun YearFortuneResultHeader(
    onBackClick: () -> Unit,
    onShareIconClick: () -> Unit,
    shareEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = DesignSystemR.drawable.ic_chevron_left),
                contentDescription = stringResource(id = DesignSystemR.string.header_back_content_description),
                tint = TodakunColor.white,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(id = R.string.year_fortune_result_header_title),
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        // ⚠️ 전용 공유 아이콘 에셋이 없어(설계 문서 2-6절) 기존 designsystem 아이콘을 임시로 사용한다.
        Box(
            modifier = Modifier.size(48.dp).clickable(enabled = shareEnabled, onClick = onShareIconClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = DesignSystemR.drawable.ic_arrow_upward),
                contentDescription = stringResource(id = R.string.year_fortune_share),
                tint = if (shareEnabled) TodakunColor.white else TodakunColor.whiteOpacity60,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun YearFortuneErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = TodakunTypography.body2Regular,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp),
        )
        PrimaryButton(
            text = stringResource(id = R.string.year_fortune_retry),
            onClick = onRetry,
            size = TodakunButtonSize.Medium,
        )
    }
}

@Composable
private fun YearScoreBadge(
    score: Int,
    headline: String,
    categories: List<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity10)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(TodakunColor.primary400),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = score.toString(), style = TodakunTypography.heading1Bold, color = TodakunColor.white)
                Text(
                    text = stringResource(id = R.string.year_fortune_score_label),
                    style = TodakunTypography.body3Regular,
                    color = TodakunColor.whiteOpacity60,
                )
            }
        }
        Text(
            text = headline,
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp, bottom = if (categories.isEmpty()) 0.dp else 20.dp),
        )
        if (categories.isNotEmpty()) {
            YearFortuneCategoryStars(categories = categories)
        }
    }
}

// ponytail: 전용 별 아이콘(ic_star_fill)이 이 워크트리의 designsystem에는 아직 없어 유니코드 별 문자로 대체.
// 아이콘 에셋이 추가되면 Icon(ic_star_fill)로 교체.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YearFortuneCategoryStars(
    categories: List<FortuneCategoryStar>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.forEach { categoryStar ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = categoryStar.category.label(),
                    style = TodakunTypography.caption1Regular,
                    color = TodakunColor.whiteOpacity60,
                )
                if (categoryStar.star > 0) {
                    Text(
                        text = "★".repeat(categoryStar.star),
                        style = TodakunTypography.body3Regular,
                        color = TodakunColor.primary400,
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneCategory.label(): String =
    stringResource(
        id =
            when (this) {
                FortuneCategory.RELATIONSHIP -> R.string.year_fortune_category_relationship
                FortuneCategory.LOVE -> R.string.year_fortune_category_love
                FortuneCategory.ACHIEVEMENT -> R.string.year_fortune_category_achievement
                FortuneCategory.MONEY -> R.string.year_fortune_category_money
                FortuneCategory.HEALTH -> R.string.year_fortune_category_health
            },
    )

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
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.year_fortune_summary_title),
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
        )
        Text(
            text = content,
            style = TodakunTypography.body2Regular,
            color = TodakunColor.whiteOpacity80,
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
                    FortuneCategoryStar(FortuneCategory.ACHIEVEMENT, 4),
                    FortuneCategoryStar(FortuneCategory.MONEY, 3),
                    FortuneCategoryStar(FortuneCategory.RELATIONSHIP, 5),
                ),
        )
    TodakunTheme {
        YearFortuneResultScreen(
            state = YearFortuneState(selectedYear = 2026, fortuneResult = sample),
            onBackClick = {},
            onShareIconClick = {},
            onShareSheetDismiss = {},
            onAskTodakClick = {},
            onRetry = {},
        )
    }
}
