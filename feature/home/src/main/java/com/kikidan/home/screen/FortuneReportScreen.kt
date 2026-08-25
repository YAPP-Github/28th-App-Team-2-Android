package com.kikidan.home.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunProgressIndicator
import com.kikidan.designsystem.component.TodakunWhiteTooltip
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.designsystem.util.noRippleClickable
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.home.component.FortuneDetailBottomSheet
import com.kikidan.home.model.CategoryScoreUiModel
import com.kikidan.home.model.FortuneReportState
import com.kikidan.home.model.HomeState
import com.kikidan.home.util.categoryLabel
import com.kikidan.home.util.characterPainter
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first

@Composable
internal fun FortuneReportScreen(
    state: FortuneReportState,
    onBackClick: () -> Unit,
    onScoreProgressRowClick: (String) -> Unit,
    onDetailDismiss: () -> Unit,
    onNavigateToLuckAction: () -> Unit,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var isTooltipVisible by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        val threshold = with(density) { 1100.dp.toPx() }
        snapshotFlow {
            scrollState.value + scrollState.viewportSize > threshold
        }.first { it }
        isTooltipVisible = true
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF00010B)),
    ) {
        Image(
            painter = painterResource(R.drawable.img_result_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier =
                Modifier
                    .matchParentSize()
                    .hazeSource(state = hazeState),
            alpha = 0.5f,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FortuneReportHeader(onBackClick = onBackClick)
            Spacer(Modifier.height(24.dp))

            when (state) {
                FortuneReportState.Loading, FortuneReportState.Failure -> {
                    Unit
                }

                is FortuneReportState.Success -> {
                    FortuneSummary(
                        scoreLabel = state.title,
                        totalScore = state.totalScore,
                    )
                    Spacer(Modifier.height(134.dp))
                    FortuneReportContentCard(
                        content = state.content,
                        totalScore = state.totalScore,
                        hazeState = hazeState,
                    )
                    Spacer(Modifier.height(20.dp))
                    FortuneReportScoreCard(
                        categories = state.categories,
                        onNavigateToLuckAction = onNavigateToLuckAction,
                        hazeState = hazeState,
                        onScoreProgressRowClick = onScoreProgressRowClick,
                    )
                    Spacer(Modifier.height(20.dp))
                    FortuneReportItemsCard(
                        title = stringResource(R.string.home_report_lucky_items_title),
                        hazeState = hazeState,
                        items = state.luckyItems,
                        painter = painterResource(R.drawable.img_home_report_luck_item),
                        chipTint = TodakunColor.sky300,
                    )
                    Spacer(Modifier.height(20.dp))
                    FortuneReportItemsCard(
                        title = stringResource(R.string.home_report_caution_items_title),
                        items = state.cautionaryItems,
                        hazeState = hazeState,
                        painter = painterResource(R.drawable.img_home_report_dangerous_item),
                        chipTint = TodakunColor.pink400,
                    )
                }
            }
            Spacer(Modifier.height(158.dp))
        }

        AskToChatFloatingButton(
            isTooltipVisible = isTooltipVisible,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .noRippleClickable(onClick = onNavigateToChat),
        )

        if (state is FortuneReportState.Success && state.detail != null) {
            FortuneDetailBottomSheet(
                detail = state.detail,
                onDismissRequest = onDetailDismiss,
                onAskTodakClick = onNavigateToChat,
            )
        }

        when (state) {
            FortuneReportState.Loading -> TodakunProgressIndicator()
            is FortuneReportState.Success, FortuneReportState.Failure -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.FortuneSummary(
    scoreLabel: String,
    totalScore: Int,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)
    LaunchedEffect(Unit) {
        tooltipState.show()
    }

    Row {
        Text(
            text = stringResource(R.string.home_report_header_title),
            style = TodakunTypography.body2Medium,
            color = TodakunColor.primary300,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(4.dp))
        TodakunWhiteTooltip(
            state = tooltipState,
            text = stringResource(R.string.home_report_tooltip_contents),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tooltip_info),
                contentDescription = null,
                tint = TodakunColor.whiteOpacity50,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = scoreLabel,
        style = TodakunTypography.heading4Bold,
        color = TodakunColor.white,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(32.dp))

    Box(
        modifier =
            Modifier
                .size(width = 105.dp, height = 64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.whiteOpacity20),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_today_score_value, totalScore),
            style = TodakunTypography.heading1ExtraBold,
            color = TodakunColor.white,
        )
    }
}

@Composable
private fun FortuneReportHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = stringResource(R.string.header_back_content_description),
            tint = TodakunColor.white,
            modifier =
                Modifier
                    .size(20.dp)
                    .clickable(onClick = onBackClick),
        )
    }
}

@Composable
private fun FortuneReportContentCard(
    content: String,
    totalScore: Int,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .hazeEffect(hazeState) { blurRadius = 20.dp },
        )

        Image(
            painter = totalScore.characterPainter(),
            contentDescription = stringResource(R.string.home_character_description),
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .height(180.dp)
                    .offset {
                        IntOffset(x = 0, y = with(density) { -90.dp.toPx().toInt() })
                    },
        )

        Text(
            modifier =
                Modifier
                    .padding(20.dp)
                    .padding(top = 112.dp),
            text = content,
            style = TodakunTypography.body1Medium,
            color = TodakunColor.white,
        )
    }
}

@Composable
private fun FortuneReportScoreCard(
    categories: PersistentList<CategoryScoreUiModel>,
    onScoreProgressRowClick: (String) -> Unit,
    hazeState: HazeState,
    onNavigateToLuckAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .hazeEffect(hazeState) {
                    blurRadius = 20.dp
                }.padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.home_report_detail_score_title),
            style = TodakunTypography.body1Bold,
            color = TodakunColor.white,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_report_detail_score_subtitle),
            style = TodakunTypography.body3Medium,
            color = TodakunColor.whiteOpacity60,
        )
        Spacer(Modifier.height(16.dp))
        categories.forEach { category ->
            CategoryScoreProgressRow(
                category = category,
                modifier =
                    Modifier.noRippleClickable(
                        onClick = { onScoreProgressRowClick(category.luckActionId) },
                    ),
            )
            Spacer(Modifier.height(16.dp))
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToLuckAction,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, TodakunColor.primary600),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Text(
                modifier = Modifier.padding(vertical = 10.dp),
                text = stringResource(R.string.home_report_lucky_action_cta),
                style = TodakunTypography.body3SemiBold,
                color = TodakunColor.primary500,
            )
        }
    }
}

@Composable
private fun CategoryScoreProgressRow(
    category: CategoryScoreUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = categoryLabel(category.category),
                style = TodakunTypography.body3SemiBold,
                color = TodakunColor.white,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = TodakunColor.white,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(TodakunColor.whiteOpacity20),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(fraction = (category.score / 100f).coerceIn(0f, 1f))
                            .height(12.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Brush.horizontalGradient(listOf(TodakunColor.sky300, TodakunColor.primary300))),
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.home_today_score_value, category.score),
                style = TodakunTypography.body2SemiBold,
                color = TodakunColor.primary200,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FortuneReportItemsCard(
    title: String,
    painter: Painter,
    hazeState: HazeState,
    items: PersistentList<String>,
    chipTint: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .hazeEffect(hazeState) { blurRadius = 20.dp }
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = TodakunTypography.body3Medium,
            color = TodakunColor.white,
        )
        Spacer(Modifier.height(20.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 38.dp),
        ) {
            items.forEach { item ->
                Box(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .background(TodakunColor.whiteOpacity10)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    Text(text = item, style = TodakunTypography.body2Regular, color = chipTint)
                }
            }
        }
    }
}

@Composable
private fun AskToChatFloatingButton(
    isTooltipVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val tooltips = stringArrayResource(R.array.home_report_floating_button_tooltips)
    val tooltipText = remember(tooltips) { tooltips.random() }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = isTooltipVisible,
            enter =
                expandHorizontally(
                    expandFrom = Alignment.End,
                ) + fadeIn(),
            exit =
                shrinkHorizontally(
                    shrinkTowards = Alignment.End,
                ) + fadeOut(),
        ) {
            Box(
                modifier =
                    Modifier.background(
                        color = TodakunColor.whiteOpacity90,
                        shape = RoundedCornerShape(99.dp),
                    ),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp),
                    text = tooltipText,
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.gray975,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            Box(
                modifier =
                    Modifier
                        .size(60.dp)
                        .border(width = 1.dp, color = TodakunColor.primary300, shape = CircleShape)
                        .clip(CircleShape),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_todak_chat_thumbnail),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment =
                        BiasAlignment(
                            horizontalBias = 0f,
                            verticalBias = 0.2f,
                        ),
                )
            }
            Image(
                modifier =
                    Modifier.offset {
                        IntOffset(x = 0, y = with(density) { -10.dp.toPx().toInt() })
                    },
                painter = painterResource(R.drawable.ic_todak_floating_bubble),
                contentDescription = null,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27, widthDp = 393, heightDp = 1400)
@Composable
private fun FortuneReportScreenPreview() {
    TodakunTheme {
        FortuneReportScreen(
            state =
                FortuneReportState.Success(
                    totalScore = 72,
                    content =
                        "오늘은 새로운 사람이나 기회가 자연스럽게 다가오는 흐름이 있어요. " +
                            "평소보다 주변의 제안에 귀를 기울여 보세요.",
                    categories =
                        persistentListOf(
                            CategoryScoreUiModel("la-1", FortuneCategory.RELATIONSHIP, 45),
                            CategoryScoreUiModel("la-2", FortuneCategory.LOVE, 84),
                            CategoryScoreUiModel("la-3", FortuneCategory.ACHIEVEMENT, 72),
                            CategoryScoreUiModel("la-4", FortuneCategory.HEALTH, 21),
                            CategoryScoreUiModel("la-5", FortuneCategory.MONEY, 90),
                        ),
                    luckyItems = listOf("노란색", "마스크", "운동화").toPersistentList(),
                    cautionaryItems = listOf("노란색", "마스크", "운동화").toPersistentList(),
                    title = "오늘은 새로운 사람이나 기회가 자연스럽게 다가오는 흐름이 있어요.",
                ),
            onBackClick = {},
            onNavigateToLuckAction = {},
            onNavigateToChat = {},
            onScoreProgressRowClick = {},
            onDetailDismiss = {},
        )
    }
}
