package com.kikidan.luckaction.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadge
import com.kikidan.designsystem.component.TodakunBadgeType
import com.kikidan.designsystem.component.TodakunCheckbox
import com.kikidan.designsystem.component.header.TodakunMainHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.luckaction.component.LuckActionCompleteOverlay
import com.kikidan.luckaction.model.FortuneScoreUiModel
import com.kikidan.luckaction.model.LuckActionItemUiModel
import com.kikidan.luckaction.model.LuckActionUiState
import java.time.LocalDate

@Composable
fun LuckActionScreen(
    state: LuckActionUiState,
    onToggleAction: (String) -> Unit,
    onPrevDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    onCompleteOverlayDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodakunMainHeader(title = stringResource(R.string.bottom_nav_lucky_action))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state) {
                    is LuckActionUiState.Loading, LuckActionUiState.Failure -> {
                        //TODO 디자인 요구사항 반영
                    }

                    is LuckActionUiState.Success -> {
                        LuckActionContent(
                            state = state,
                            onToggleAction = onToggleAction,
                            onPrevDateClick = onPrevDateClick,
                            onNextDateClick = onNextDateClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        if (state is LuckActionUiState.Success) {
            state.completionOverlayCategory?.let { category ->
                LuckActionCompleteOverlay(
                    category = category,
                    onCloseClick = onCompleteOverlayDismiss,
                )
            }
        }
    }
}

@Composable
private fun LuckActionContent(
    state: LuckActionUiState.Success,
    onToggleAction: (String) -> Unit,
    onPrevDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 20.dp),
    ) {
        item { LuckActionDescription() }
        item {
            FortuneScoreRow(
                scores = state.scores,
                modifier = Modifier.padding(top = 32.dp),
            )
        }
        item {
            LuckActionDateHeader(
                date = state.date,
                isPrevEnabled = state.canGoToPrevDate,
                isRefreshing = state.isRefreshing,
                onPrevDateClick = onPrevDateClick,
                onNextDateClick = onNextDateClick,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        items(state.actions, key = { it.id }) { action ->
            LuckActionItemCard(
                item = action,
                onToggle = onToggleAction,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun LuckActionDescription(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.luck_action_description),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.gray975,
        )
        Image(
            painter = painterResource(R.drawable.img_luck_action_intro),
            contentDescription = stringResource(R.string.luck_action_character_content_description),
            modifier = Modifier.size(92.dp),
        )
    }
}

@Composable
private fun FortuneScoreRow(
    scores: List<FortuneScoreUiModel>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.primary100)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        scores.forEachIndexed { index, score ->
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = score.category.scoreLabel(),
                    style = TodakunTypography.caption3SemiBold,
                    color = TodakunColor.gray975,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(99.dp))
                            .background(TodakunColor.whiteOpacity90)
                            .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = score.score.toString(),
                        style = TodakunTypography.body3SemiBold,
                        color = TodakunColor.primary700,
                    )
                }
            }
            if (index != scores.lastIndex) {
                Box(
                    modifier =
                        Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(TodakunColor.blackOpacity05),
                )
            }
        }
    }
}

@Composable
private fun LuckActionDateHeader(
    date: LocalDate,
    isPrevEnabled: Boolean,
    isRefreshing: Boolean,
    onPrevDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isNextEnabled = date.isBefore(LocalDate.now())
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.luck_action_date_title, date.monthValue, date.dayOfMonth),
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.gray975,
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = TodakunColor.primary600,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.luck_action_prev_date_content_description),
                tint = if (isPrevEnabled) TodakunColor.gray975 else TodakunColor.gray300,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clickable(enabled = isPrevEnabled, onClick = onPrevDateClick),
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.luck_action_next_date_content_description),
                tint = if (isNextEnabled) TodakunColor.gray975 else TodakunColor.gray300,
                modifier =
                    Modifier
                        .size(24.dp)
                        .scale(scaleX = -1f, scaleY = 1f)
                        .clickable(enabled = isNextEnabled, onClick = onNextDateClick),
            )
        }
    }
}

@Composable
private fun LuckActionItemCard(
    item: LuckActionItemUiModel,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.coolGray50)
                .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TodakunBadge(text = item.category.badgeLabel(), type = item.category.badgeType())
            Text(
                text = item.title,
                style = TodakunTypography.body3SemiBold,
                color = TodakunColor.gray975,
            )
        }
        TodakunCheckbox(
            checked = item.achieved,
            onCheckedChange = { onToggle(item.id) },
        )
    }
}

@Composable
private fun FortuneCategory.scoreLabel(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_score_relationship
            FortuneCategory.LOVE -> R.string.luck_action_score_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_score_achievement
            FortuneCategory.MONEY -> R.string.luck_action_score_money
            FortuneCategory.HEALTH -> R.string.luck_action_score_health
        },
    )

@Composable
private fun FortuneCategory.badgeLabel(): String =
    stringResource(
        when (this) {
            FortuneCategory.RELATIONSHIP -> R.string.luck_action_badge_relationship
            FortuneCategory.LOVE -> R.string.luck_action_badge_love
            FortuneCategory.ACHIEVEMENT -> R.string.luck_action_badge_achievement
            FortuneCategory.MONEY -> R.string.luck_action_badge_money
            FortuneCategory.HEALTH -> R.string.luck_action_badge_health
        },
    )

private fun FortuneCategory.badgeType(): TodakunBadgeType =
    when (this) {
        FortuneCategory.RELATIONSHIP -> TodakunBadgeType.Purple
        FortuneCategory.LOVE -> TodakunBadgeType.Pink
        FortuneCategory.ACHIEVEMENT -> TodakunBadgeType.Green
        FortuneCategory.MONEY -> TodakunBadgeType.Blue
        FortuneCategory.HEALTH -> TodakunBadgeType.Yellow
    }

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun LuckActionScreenPreview() {
    TodakunTheme {
        LuckActionScreen(
            state =
                LuckActionUiState.Success(
                    date = LocalDate.now(),
                    canGoToPrevDate = true,
                    scores =
                        listOf(
                            FortuneScoreUiModel(FortuneCategory.RELATIONSHIP, 84),
                            FortuneScoreUiModel(FortuneCategory.LOVE, 21),
                            FortuneScoreUiModel(FortuneCategory.ACHIEVEMENT, 17),
                            FortuneScoreUiModel(FortuneCategory.HEALTH, 60),
                            FortuneScoreUiModel(FortuneCategory.MONEY, 93),
                        ),
                    actions =
                        listOf(
                            LuckActionItemUiModel("1", FortuneCategory.RELATIONSHIP, "오랜만에 생각난 사람에게 메시지 보내기", false),
                            LuckActionItemUiModel("2", FortuneCategory.LOVE, "평소보다 밝은 컬러의 옷 착용하기", true),
                            LuckActionItemUiModel("3", FortuneCategory.ACHIEVEMENT, "미뤄둔 작은 업무 하나 먼저 끝내기", false),
                            LuckActionItemUiModel("4", FortuneCategory.HEALTH, "10분 정도 가볍게 산책하거나 스트레칭하기", false),
                            LuckActionItemUiModel("5", FortuneCategory.MONEY, "사용하지 않는 구독 서비스나 자동결제 내역 확인하기", false),
                        ),
                ),
            onToggleAction = {},
            onPrevDateClick = {},
            onNextDateClick = {},
            onCompleteOverlayDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LuckActionScreenOverlayPreview() {
    TodakunTheme {
        LuckActionScreen(
            state =
                LuckActionUiState.Success(
                    date = LocalDate.now(),
                    canGoToPrevDate = true,
                    completionOverlayCategory = FortuneCategory.LOVE,
                ),
            onToggleAction = {},
            onPrevDateClick = {},
            onNextDateClick = {},
            onCompleteOverlayDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LuckActionScreenLoadingPreview() {
    TodakunTheme {
        LuckActionScreen(
            state = LuckActionUiState.Loading,
            onToggleAction = {},
            onPrevDateClick = {},
            onNextDateClick = {},
            onCompleteOverlayDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LuckActionScreenFailurePreview() {
    TodakunTheme {
        LuckActionScreen(
            state = LuckActionUiState.Failure,
            onToggleAction = {},
            onPrevDateClick = {},
            onNextDateClick = {},
            onCompleteOverlayDismiss = {},
        )
    }
}
