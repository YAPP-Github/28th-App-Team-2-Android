package com.kikidan.home.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
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
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun HomeScreen(
    state: HomeState,
    onCategoryClick: (String) -> Unit,
    onDetailDismiss: () -> Unit,
    onNavigateToLuckAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.img_result_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize()
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            when (state) {
                // 로딩/실패 화면은 추후 공용 처리로 별도 구현 예정 (TODO(#38))
                HomeState.Loading, HomeState.Failure -> {
                    Unit
                }

                is HomeState.Success -> {
                    HomeDarkSection(
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

        if (state is HomeState.Success && state.detail != null) {
            FortuneDetailBottomSheet(
                detail = state.detail,
                onDismissRequest = onDetailDismiss,
            )
        }
    }
}

@Composable
private fun HomeDarkSection(
    totalScore: Int,
    scoreLabel: String,
    onFortuneReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
        ) {
            HomeHeader()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (scoreLabel.isNotEmpty()) {
                    Text(
                        text = scoreLabel,
                        style = TodakunTypography.heading3Bold,
                        color = TodakunColor.white,
                        modifier = Modifier.weight(1f).padding(bottom = 24.dp),
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Image(
                    painter = painterResource(R.drawable.img_home_character),
                    contentDescription = stringResource(R.string.home_character_description),
                    contentScale = ContentScale.Fit,
                    modifier =
                        Modifier.height(102.dp),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (scoreLabel.isNotEmpty()) {
                HomeTodayScoreCard(
                    totalScore = totalScore,
                    onFortuneReportClick = onFortuneReportClick,
                    modifier =
                        Modifier.drawBackdrop(
                            backdrop = rememberLayerBackdrop(),
                            shape = { RoundedCornerShape(16.dp) },
                            effects = {
                                vibrancy()
                                blur(Glass.Frost.toPx())
                                lens(
                                    refractionHeight = Glass.Depth.toPx(),
                                    refractionAmount = Glass.Refraction.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true,
                                )
                            },
                            onDrawSurface = { drawRect(TodakunColor.whiteOpacity10) },
                        ),
                )
            }

            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.img_logo_light_color_gradient),
            contentDescription = null,
            modifier = Modifier.height(24.dp),
        )
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
    ) {
        Spacer(modifier = Modifier.height(36.dp))
        HomeCategoryScoreRow(
            categories = state.categories,
            onCategoryClick = onCategoryClick,
        )
        Spacer(Modifier.height(44.dp))
        HomeSajuSection()
        Spacer(Modifier.height(30.dp))
        HomeLuckActionBanner(
            onNavigateToLuckAction = onNavigateToLuckAction,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(72.dp))
    }
}

private object Glass {
    val Frost = 15.dp
    val Depth = 20.dp
    val Refraction = 80.dp     // refraction 80  (0~100 → 0~50dp)
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E27, widthDp = 393, heightDp = 852)
@Composable
private fun HomeScreenSuccessPreview() {
    TodakunTheme {
        HomeScreen(
            state =
                HomeState.Success(
                    totalScore = 72,
                    scoreLabel = "흐름 좋은 날",
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
