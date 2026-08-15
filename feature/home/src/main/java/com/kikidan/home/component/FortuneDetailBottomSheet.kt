package com.kikidan.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.home.model.DetailSheetUiState
import com.kikidan.home.util.categoryLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FortuneDetailBottomSheet(
    detail: DetailSheetUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenHeight =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.height
                .toDp()
        }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TodakunColor.white,
        dragHandle = { },
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier.heightIn(max = screenHeight * 0.8f),
        ) {
            SheetHeader(
                detail = detail,
                onDismissRequest = onDismissRequest,
            )
            when (detail) {
                is DetailSheetUiState.Loading -> {
                    Unit
                }

                is DetailSheetUiState.Success -> {
                    Column(
                        modifier =
                            Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                    ) {
                        Spacer(Modifier.height(30.dp))
                        FortuneScoreGauge(
                            score = detail.score,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                        Spacer(Modifier.height(32.dp))
                        LuckActionBox(actionTitle = detail.actionTitle)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = detail.content,
                            style = TodakunTypography.body3Regular,
                            color = TodakunColor.gray975,
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        PrimaryButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.home_detail_sheet_cta),
                            onClick = {
                                // TODO(#38): 챗봇 연결 미구현, 머지 후 연결
                            },
                            size = TodakunButtonSize.Large,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(
    detail: DetailSheetUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val category =
        when (detail) {
            is DetailSheetUiState.Loading -> detail.category
            is DetailSheetUiState.Success -> detail.category
        }
    Column(
        modifier =
            modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = onDismissRequest,
            modifier =
                Modifier
                    .align(Alignment.End)
                    .padding(
                        top = 24.dp,
                    ).padding(
                        horizontal = 24.dp,
                    ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.header_close_content_description),
                tint = TodakunColor.gray975,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = stringResource(R.string.home_detail_sheet_title_format, categoryLabel(category)),
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.gray975,
            modifier =
                Modifier
                    .align(Alignment.Start)
                    .padding(
                        vertical = 12.dp,
                        horizontal = 20.dp,
                    ),
        )
    }
}

@Composable
private fun LuckActionBox(
    actionTitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TodakunColor.gray25)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_detail_today_action),
            style = TodakunTypography.body3Medium,
            color = TodakunColor.primary600,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = actionTitle,
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.gray975,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun FortuneDetailBottomSheetSuccessPreview() {
    TodakunTheme {
        FortuneDetailBottomSheet(
            detail =
                DetailSheetUiState.Success(
                    category = FortuneCategory.LOVE,
                    score = 84,
                    actionTitle = "오늘 소중한 사람에게 연락해보세요",
                    content = "오늘은 감정 표현이 풍부해지는 날이에요. 주변 사람들과의 교류가 활발해지고, 새로운 인연이 생길 수도 있어요.",
                ),
            onDismissRequest = {},
        )
    }
}
