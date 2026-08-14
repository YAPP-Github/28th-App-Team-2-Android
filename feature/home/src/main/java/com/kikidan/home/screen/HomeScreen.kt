package com.kikidan.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.bottomnavigation.TodakunBottomNavigation
import com.kikidan.designsystem.component.bottomnavigation.TodakunNavItem
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.home.component.FortuneDetailBottomSheet
import com.kikidan.home.component.HomeCategoryScoreRow
import com.kikidan.home.component.HomeLuckActionBanner
import com.kikidan.home.component.HomeSajuSection
import com.kikidan.home.component.HomeTodayScoreCard
import com.kikidan.home.model.CategoryScoreUiModel
import com.kikidan.home.model.HomeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.persistentListOf

private val HomeDarkBackground =
    Brush.verticalGradient(listOf(TodakunColor.coolGray900, TodakunColor.primary900))

@Composable
internal fun HomeScreen(
    state: HomeState,
    onCategoryClick: (String) -> Unit,
    onDetailDismiss: () -> Unit,
    onNavigateToLuckAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader()
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
            ) {
                when (state) {
                    HomeState.Loading -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(HomeDarkBackground)
                                    .padding(vertical = 80.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = TodakunColor.white)
                        }
                    }

                    HomeState.Failure -> {
                        HomeFailureContent()
                    }

                    is HomeState.Success -> {
                        HomeDarkSection(
                            greeting = state.greeting,
                            totalScore = state.totalScore,
                            scoreLabel = state.scoreLabel,
                            onFortuneReportClick = {
                                // TODO(#38): feature:fortune-report 머지 후 연결
                            },
                        )
                        HomeWhiteSection(
                            state = state,
                            onCategoryClick = onCategoryClick,
                            onNavigateToLuckAction = onNavigateToLuckAction,
                        )
                    }
                }
            }
            TodakunBottomNavigation(
                selectedItem = TodakunNavItem.FORTUNE_TELLING,
                onItemSelect = {
                    // TODO(#38): NavHost 배선 후 연결
                },
            )
        }

        if (state is HomeState.Success && state.detail != null) {
            FortuneDetailBottomSheet(
                detail = state.detail,
                onDismissRequest = onDetailDismiss,
            )
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HomeDarkBackground)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo_light_color),
            contentDescription = null,
            modifier = Modifier.height(24.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {
            // TODO(#38): 알림 화면 미구현, 머지 후 연결
        }) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = stringResource(R.string.header_notice_content_description),
                tint = TodakunColor.white,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun HomeDarkSection(
    greeting: String,
    totalScore: Int,
    scoreLabel: String,
    onFortuneReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hazeState = rememberHazeState()
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(HomeDarkBackground)
                .hazeSource(hazeState),
    ) {
        // 캐릭터 이미지: TODO(#38) Figma export 후 img_home_character로 교체
        Image(
            painter = painterResource(R.drawable.img_todak_default_pose),
            contentDescription = stringResource(R.string.home_character_description),
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 20.dp)
                    .height(200.dp),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 24.dp),
        ) {
            if (greeting.isNotEmpty()) {
                Text(
                    text = greeting,
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.white,
                    modifier = Modifier.padding(bottom = 24.dp, end = 120.dp),
                )
            }
            if (scoreLabel.isNotEmpty()) {
                HomeTodayScoreCard(
                    totalScore = totalScore,
                    scoreLabel = scoreLabel,
                    onFortuneReportClick = onFortuneReportClick,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .hazeEffect(
                                hazeState,
                                HazeStyle(
                                    tints = listOf(HazeTint(TodakunColor.whiteOpacity10)),
                                    blurRadius = 8.dp,
                                ),
                            ),
                )
            }
        }
    }
}

@Composable
private fun HomeWhiteSection(
    state: HomeState.Success,
    onCategoryClick: (String) -> Unit,
    onNavigateToLuckAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(TodakunColor.white)
                .padding(vertical = 24.dp),
    ) {
        HomeCategoryScoreRow(
            categories = state.categories,
            onCategoryClick = onCategoryClick,
        )
        Spacer(Modifier.height(32.dp))
        HomeSajuSection()
        Spacer(Modifier.height(24.dp))
        HomeLuckActionBanner(
            onNavigateToLuckAction = onNavigateToLuckAction,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HomeFailureContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(HomeDarkBackground),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_failure_message),
            style = TodakunTypography.body2Regular,
            color = TodakunColor.whiteOpacity60,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenSuccessPreview() {
    TodakunTheme {
        HomeScreen(
            state =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
                    greeting = stringResource(R.string.home_greeting),
                    categories =
                        persistentListOf(
                            CategoryScoreUiModel("la-1", FortuneCategory.RELATIONSHIP, 45),
                            CategoryScoreUiModel("la-2", FortuneCategory.LOVE, 84),
                            CategoryScoreUiModel("la-3", FortuneCategory.ACHIEVEMENT, 38),
                            CategoryScoreUiModel("la-4", FortuneCategory.HEALTH, 21),
                            CategoryScoreUiModel("la-5", FortuneCategory.MONEY, 72),
                        ),
                ),
            onCategoryClick = {},
            onDetailDismiss = {},
            onNavigateToLuckAction = {},
        )
    }
}
