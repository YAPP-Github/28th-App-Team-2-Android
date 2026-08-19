package com.kikidan.mypage.mansaeryeok.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.CheonGan
import com.kikidan.domain.model.saju.JiJi
import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.SajuPillar
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus
import com.kikidan.domain.model.user.User
import com.kikidan.mypage.mansaeryeok.model.FiveElementDistribution
import com.kikidan.mypage.mansaeryeok.model.FiveElementStatus
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailUiModel
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailUiState
import com.kikidan.mypage.mansaeryeok.model.SajuPillarDetail
import com.kikidan.mypage.mansaeryeok.model.TenGod
import com.kikidan.mypage.mansaeryeok.model.TwelveSinsal
import com.kikidan.mypage.mansaeryeok.model.TwelveUnseong
import com.kikidan.mypage.mansaeryeok.ui.component.FiveElementCard
import com.kikidan.mypage.mansaeryeok.ui.component.MansaeryeokInfoBottomSheet
import com.kikidan.mypage.mansaeryeok.ui.component.SajuFourPillarsTable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MansaeryeokDetailScreen(
    uiState: MansaeryeokDetailUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white)
                .systemBarsPadding(),
    ) {
        TodakunSubHeader(
            title = stringResource(R.string.mansaeryeok_detail_title),
            onBackClick = onBackClick,
        )

        when (uiState) {
            is MansaeryeokDetailUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
            }

            is MansaeryeokDetailUiState.Fail -> {
                Box(modifier = Modifier.weight(1f).fillMaxSize())
            }

            is MansaeryeokDetailUiState.Success -> {
                MansaeryeokDetailContent(
                    model = uiState.model,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MansaeryeokDetailContent(
    model: MansaeryeokDetailUiModel,
    modifier: Modifier = Modifier,
) {
    var showSajuWonGukInfo by remember { mutableStateOf(false) }
    var showOhaengInfo by remember { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        ProfileSummaryCard(user = model.user)
        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel(
            text = stringResource(R.string.mansaeryeok_saju_won_guk_title),
            onInfoClick = { showSajuWonGukInfo = true },
        )
        Spacer(modifier = Modifier.height(16.dp))
        SajuFourPillarsTable(pillars = model.pillars)
        Spacer(modifier = Modifier.height(32.dp))

        SectionLabel(
            text = stringResource(R.string.mansaeryeok_ohaeng_title),
            onInfoClick = { showOhaengInfo = true },
        )
        Spacer(modifier = Modifier.height(16.dp))
        FiveElementCard(fiveElements = model.fiveElements)
    }

    if (showSajuWonGukInfo) {
        MansaeryeokInfoBottomSheet(
            titleRes = R.string.mansaeryeok_saju_won_guk_info_title,
            descriptionRes = R.string.mansaeryeok_saju_won_guk_info_description,
            pointRes =
                listOf(
                    R.string.mansaeryeok_saju_won_guk_info_year,
                    R.string.mansaeryeok_saju_won_guk_info_month,
                    R.string.mansaeryeok_saju_won_guk_info_day,
                    R.string.mansaeryeok_saju_won_guk_info_hour,
                ),
            onDismissRequest = { showSajuWonGukInfo = false },
        )
    }

    if (showOhaengInfo) {
        MansaeryeokInfoBottomSheet(
            titleRes = R.string.mansaeryeok_ohaeng_info_title,
            descriptionRes = R.string.mansaeryeok_ohaeng_info_description,
            pointRes =
                listOf(
                    R.string.mansaeryeok_ohaeng_info_mok,
                    R.string.mansaeryeok_ohaeng_info_hwa,
                    R.string.mansaeryeok_ohaeng_info_to,
                    R.string.mansaeryeok_ohaeng_info_geum,
                    R.string.mansaeryeok_ohaeng_info_su,
                ),
            onDismissRequest = { showOhaengInfo = false },
        )
    }
}

@Composable
private fun ProfileSummaryCard(
    user: User,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.coolGray50)
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 18.dp),
    ) {
        Text(
            text = stringResource(R.string.mansaeryeok_profile_format, user.name, user.gender.displayName()),
            style = TodakunTypography.body2SemiBold,
            color = TodakunColor.gray975,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = user.birth.displayText(),
            style = TodakunTypography.body3Regular,
            color = TodakunColor.gray975,
        )
    }
}

@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            style = TodakunTypography.body1Bold,
            color = TodakunColor.black,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_circle_info),
            contentDescription = stringResource(R.string.mansaeryeok_info_content_description),
            tint = TodakunColor.gray400,
            modifier =
                Modifier
                    .size(20.dp)
                    .let { iconModifier ->
                        if (onInfoClick != null) iconModifier.clickable(onClick = onInfoClick) else iconModifier
                    },
        )
    }
}

@Composable
private fun Gender.displayName(): String =
    when (this) {
        Gender.MALE -> stringResource(R.string.gender_male)
        Gender.FEMALE -> stringResource(R.string.gender_female)
    }

@Composable
private fun Birth.displayText(): String {
    val dateText = date.format(BirthDateFormatter)
    val dateTypeText = dateType.displayName()

    if (time == BirthTime.UNKNOWN) {
        return stringResource(R.string.mansaeryeok_profile_birth_date_only_format, dateText, dateTypeText)
    }

    val start = time.startTime
    val end = time.endTime
    if (start == null || end == null) {
        return stringResource(R.string.mansaeryeok_profile_birth_date_only_format, dateText, dateTypeText)
    }

    val timeRange =
        stringResource(
            R.string.mansaeryeok_time_range_format,
            stringResource(R.string.mansaeryeok_time_colon_format, start.hour, start.minute),
            stringResource(
                R.string.mansaeryeok_time_colon_format,
                end.minusMinutes(1).hour,
                end.minusMinutes(1).minute,
            ),
        )

    return stringResource(
        R.string.mansaeryeok_profile_birth_format,
        dateText,
        dateTypeText,
        timeRange,
        time.displayName,
    )
}

@Composable
private fun DateType.displayName(): String =
    when (this) {
        DateType.SOLAR -> stringResource(R.string.date_type_solar)
        DateType.LUNAR -> stringResource(R.string.date_type_lunar)
    }

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Preview(showBackground = true)
@Composable
private fun MansaeryeokDetailScreenPreview() {
    TodakunTheme {
        MansaeryeokDetailScreen(
            uiState = MansaeryeokDetailUiState.Success(PreviewMansaeryeokDetail),
        )
    }
}

private val PreviewMansaeryeokDetail =
    MansaeryeokDetailUiModel(
        user =
            User(
                id = "preview",
                name = "토닥이",
                gender = Gender.FEMALE,
                job = Job.WORKER,
                relationshipStatus = RelationshipStatus.SOLO,
                birth =
                    Birth(
                        dateType = DateType.SOLAR,
                        date = LocalDate.of(2001, 5, 30),
                        time = BirthTime.O,
                    ),
            ),
        pillars =
            listOf(
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.SIN, JiJi.MI),
                    pillarLabelRes = R.string.mansaeryeok_pillar_hour,
                    periodLabelRes = R.string.mansaeryeok_period_late,
                    topTenGod = TenGod.SIKSIN,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "을계무",
                    twelveUnseong = TwelveUnseong.YANG,
                    twelveSinsal = TwelveSinsal.HWAGAESAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.GI, JiJi.SA),
                    pillarLabelRes = R.string.mansaeryeok_pillar_day,
                    periodLabelRes = R.string.mansaeryeok_period_prime,
                    topTenGod = TenGod.ILWON,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "무경병",
                    twelveUnseong = TwelveUnseong.TAE,
                    twelveSinsal = TwelveSinsal.GEOPSAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.GYE, JiJi.MYO),
                    pillarLabelRes = R.string.mansaeryeok_pillar_month,
                    periodLabelRes = R.string.mansaeryeok_period_youth,
                    topTenGod = TenGod.PYEONJAE,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "계신기",
                    twelveUnseong = TwelveUnseong.GWANDAE,
                    twelveSinsal = TwelveSinsal.BANANSAL,
                ),
                SajuPillarDetail(
                    pillar = SajuPillar(CheonGan.JEONG, JiJi.CHUK),
                    pillarLabelRes = R.string.mansaeryeok_pillar_year,
                    periodLabelRes = R.string.mansaeryeok_period_early,
                    topTenGod = TenGod.PYEONIN,
                    bottomTenGod = TenGod.BIGYEON,
                    hiddenStem = "을계무",
                    twelveUnseong = TwelveUnseong.YANG,
                    twelveSinsal = TwelveSinsal.HWAGAESAL,
                ),
            ),
        fiveElements =
            listOf(
                FiveElementDistribution(Ohaeng.MOK, count = 1, status = FiveElementStatus.LACKING),
                FiveElementDistribution(Ohaeng.HWA, count = 2, status = FiveElementStatus.MODERATE),
                FiveElementDistribution(Ohaeng.TO, count = 3, status = FiveElementStatus.ABUNDANT),
                FiveElementDistribution(Ohaeng.GEUM, count = 1, status = FiveElementStatus.MODERATE),
                FiveElementDistribution(Ohaeng.SU, count = 1, status = FiveElementStatus.LACKING),
            ),
    )
