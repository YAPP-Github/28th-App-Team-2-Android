package com.kikidan.mypage.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
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
import com.kikidan.mypage.ui.component.SajuPaljaGrid
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun ProfileCard(
    user: User,
    sajuPalja: SajuPalja,
    onEditClick: () -> Unit,
    onViewMansaeryeokClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TodakunColor.white)
                .border(1.dp, TodakunColor.gray100, RoundedCornerShape(20.dp))
                .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        style = TodakunTypography.heading3Bold,
                        color = TodakunColor.gray975,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.separator_dot),
                        style = TodakunTypography.body2Regular,
                        color = TodakunColor.gray700,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user.gender.displayName(),
                        style = TodakunTypography.body2Regular,
                        color = TodakunColor.gray700,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                UserBirthDateTime(birth = user.birth)
            }
            EditButton(onClick = onEditClick)
        }

        Spacer(modifier = Modifier.height(20.dp))
        SajuPaljaGrid(sajuPalja = sajuPalja)
        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = stringResource(R.string.mypage_view_mansaeryeok_button),
            onClick = onViewMansaeryeokClick,
            size = TodakunButtonSize.Medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UserBirthDateTime(birth: Birth) {
    val dateText = "${birth.date.format(BirthDateFormatter)} ${birth.dateType.displayName()}"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = dateText,
            style = TodakunTypography.body3Medium,
            color = TodakunColor.gray700,
        )
        if (birth.time != BirthTime.UNKNOWN) {
            Text(
                text = stringResource(R.string.separator_dot),
                style = TodakunTypography.body3Medium,
                color = TodakunColor.gray700,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Text(
                text = birth.time.displayText(),
                style = TodakunTypography.body3Medium,
                color = TodakunColor.gray700,
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun UserBirthDateTimePreview() {
    val birth =
        Birth(
            dateType = DateType.SOLAR,
            date = LocalDate.of(1999, 2, 13),
            time = BirthTime.SIN,
        )
    UserBirthDateTime(
        birth = birth,
    )
}

@Composable
private fun EditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    TodakunColor.primary500,
                    RoundedCornerShape(8.dp),
                ).clickable(onClick = onClick)
                .background(TodakunColor.white)
                .padding(horizontal = 19.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.mypage_edit_button),
            style = TodakunTypography.caption1SemiBold,
            color = TodakunColor.primary700,
        )
    }
}

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Composable
private fun Gender.displayName(): String =
    when (this) {
        Gender.MALE -> stringResource(R.string.gender_male)
        Gender.FEMALE -> stringResource(R.string.gender_female)
    }

@Composable
private fun DateType.displayName(): String =
    when (this) {
        DateType.SOLAR -> stringResource(R.string.date_type_solar)
        DateType.LUNAR -> stringResource(R.string.date_type_lunar)
    }

@Composable
private fun BirthTime.displayText(): String {
    val start = startTime ?: return ""
    return stringResource(R.string.birth_time_format, start.hour, start.minute)
}

@Preview(showBackground = true)
@Composable
private fun ProfileCardPreview() {
    TodakunTheme {
        ProfileCard(
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
            onEditClick = {},
            onViewMansaeryeokClick = {},
        )
    }
}
