package com.kikidan.mypage.home.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunDivider
import com.kikidan.designsystem.component.TodakunDividerType
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.SajuPalja
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.User
import com.kikidan.mypage.home.model.MyPageHomeUiModel
import com.kikidan.mypage.home.model.MyPageHomeUiState
import com.kikidan.mypage.home.model.MyPageMenuType
import com.kikidan.mypage.home.ui.component.MyPageHomeHeader
import com.kikidan.mypage.home.ui.component.MyPageMenuItem
import com.kikidan.mypage.home.ui.component.ProfileCard
import java.time.LocalDate

@Composable
fun MyPageHomeScreen(
    uiState: MyPageHomeUiState,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onViewMansaeryeokClick: () -> Unit = {},
    onMenuItemClick: (MyPageMenuType) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        MyPageHomeHeader(modifier = Modifier.fillMaxWidth())

        when (uiState) {
            is MyPageHomeUiState.Loading -> {
                MyPageHomeLoading(modifier = Modifier.weight(1f))
            }

            is MyPageHomeUiState.Fail -> {
                MyPageHomeError(modifier = Modifier.weight(1f))
            }

            is MyPageHomeUiState.Success -> {
                MyPageHomeContent(
                    model = uiState.model,
                    onEditClick = onEditClick,
                    onViewMansaeryeokClick = onViewMansaeryeokClick,
                    onMenuItemClick = onMenuItemClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MyPageHomeLoading(modifier: Modifier = Modifier) {
    // 로딩 요구사항 없음
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    }
}

@Composable
private fun MyPageHomeError(modifier: Modifier = Modifier) {
    // 에러 요구사항 없음
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    }
}

@Composable
private fun MyPageHomeContent(
    model: MyPageHomeUiModel,
    onEditClick: () -> Unit,
    onViewMansaeryeokClick: () -> Unit,
    onMenuItemClick: (MyPageMenuType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ProfileCard(
            user = model.user,
            sajuPalja = model.sajuPalja,
            onEditClick = onEditClick,
            onViewMansaeryeokClick = onViewMansaeryeokClick,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        TodakunDivider(type = TodakunDividerType.Section)

        MyPageMenuType.entries.forEach { menuType ->
            MyPageMenuItem(
                painter = painterResource(menuType.iconRes),
                label = stringResource(menuType.labelRes),
                onClick = { onMenuItemClick(menuType) },
                showChevron = menuType.showChevron,
                iconTint = menuType.iconTint,
                textColor = menuType.textColor,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.mypage_app_version_label),
                style = TodakunTypography.body3Medium,
                color = TodakunColor.gray400,
            )
            Spacer(modifier = Modifier.width(16.dp))
            val versionText =
                stringResource(R.string.mypage_app_version_format, model.appVersionName) +
                    if (model.isLatestVersion) " " + stringResource(R.string.mypage_latest_version) else ""
            Text(
                text = versionText,
                style = TodakunTypography.body3Medium,
                color = TodakunColor.gray400,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPageHomeScreenPreview() {
    TodakunTheme {
        MyPageHomeScreen(
            uiState =
                MyPageHomeUiState.Success(
                    MyPageHomeUiModel(
                        user =
                            User(
                                id = "preview",
                                name = "토닥이",
                                gender = Gender.FEMALE,
                                birth =
                                    Birth(
                                        dateType = DateType.SOLAR,
                                        date = LocalDate.of(1999, 2, 13),
                                        time = BirthTime.SIN,
                                    ),
                            ),
                        sajuPalja =
                            SajuPalja(
                                yearPillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                                monthPillar = SajuPillar(CheonGan.GI, JiJi.SA),
                                dayPillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                                hourPillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                            ),
                        appVersionName = "1.0.0",
                        isLatestVersion = false,
                    ),
                ),
        )
    }
}
