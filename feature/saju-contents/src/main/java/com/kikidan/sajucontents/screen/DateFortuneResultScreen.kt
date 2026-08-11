package com.kikidan.sajucontents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunChip2
import com.kikidan.designsystem.component.TodakunProgressBar
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.designsystem.R
import com.kikidan.sajucontents.component.FortuneScoreCard
import com.kikidan.sajucontents.component.ResultDateTabRow
import com.kikidan.sajucontents.model.DateFortuneState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate

// 입력 화면과 동일한 진행률(Q10, 결과 화면의 "완료" 단계 표현이 Figma에 없음).
private const val RESULT_PROGRESS = 210f / 316f

@Composable
internal fun DateFortuneResultScreen(
    state: DateFortuneState,
    onBackClick: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onShareClick: () -> Unit,
    onExportClick: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedFortune = state.results.getOrNull(state.selectedResultIndex)

    // 배경 이미지 에셋이 아직 없어(N5) night 단색 배경으로 플레이스홀더 처리한다.
    Box(modifier = modifier.fillMaxSize().background(TodakunColor.night)) {
        if (selectedFortune == null) {
            CircularProgressIndicator(
                color = TodakunColor.white,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TodakunProgressBar(
                    progress = RESULT_PROGRESS,
                    onBackClick = onBackClick,
                    modifier = Modifier.weight(1f),
                )
                // ⚠️ 전용 아이콘 에셋(ic_event_export/ic_share)이 아직 없다(N5). 임시로 기존 designsystem 아이콘을 사용한다.
                Icon(
                    painter = painterResource(id = R.drawable.ic_notes),
                    contentDescription = stringResource(id = R.string.date_fortune_export_calendar),
                    tint = TodakunColor.white,
                    modifier = Modifier.size(24.dp).clickable(onClick = onExportClick),
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_upward),
                    contentDescription = stringResource(id = R.string.date_fortune_share),
                    tint = TodakunColor.white,
                    modifier = Modifier.size(24.dp).padding(start = 12.dp).clickable(onClick = onShareClick),
                )
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text(
                    text = stringResource(id = R.string.date_fortune_result_title),
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.white,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                TodakunChip2(text = selectedFortune.purpose.label())
                Text(
                    text = stringResource(id = R.string.date_fortune_result_subtitle),
                    style = TodakunTypography.body1Regular,
                    color = TodakunColor.white,
                    modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
                )
                ResultDateTabRow(
                    results = state.results,
                    selectedIndex = state.selectedResultIndex,
                    onTabSelect = onTabSelect,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
                FortuneScoreCard(
                    score = selectedFortune.score,
                    title = selectedFortune.title,
                    categoryStars = selectedFortune.categoryStars.toImmutableList(),
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                SummaryCard(content = selectedFortune.content, modifier = Modifier.padding(bottom = 24.dp))
                PrimaryButton(
                    text = stringResource(id = R.string.date_fortune_ask_todak),
                    onClick = onAskTodakClick,
                    size = TodakunButtonSize.Large,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                )
            }
        }
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
                .background(TodakunColor.white.copy(alpha = 0.1f))
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.date_fortune_summary_title),
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.white,
        )
        Text(
            text = content,
            style = TodakunTypography.body2Regular,
            color = TodakunColor.white.copy(alpha = 0.8f),
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
            categoryStars = emptyList(),
        )
    TodakunTheme {
        DateFortuneResultScreen(
            state =
                DateFortuneState(
                    results = persistentListOf(sampleFortune),
                    selectedResultIndex = 0,
                ),
            onBackClick = {},
            onTabSelect = {},
            onShareClick = {},
            onExportClick = {},
            onAskTodakClick = {},
        )
    }
}
