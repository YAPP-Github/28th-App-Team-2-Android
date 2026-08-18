package com.kikidan.mypage.partner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunBadge
import com.kikidan.designsystem.component.TodakunBadgeType
import com.kikidan.designsystem.component.TodakunPopover
import com.kikidan.designsystem.component.TodakunSnackbar
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.mypage.partner.model.PartnerSajuManagementUiModel
import com.kikidan.mypage.partner.model.PartnerSajuManagementUiState
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun PartnerSajuManagementScreen(
    uiState: PartnerSajuManagementUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddPartnerClick: () -> Unit = {},
    onEditPartnerClick: (String) -> Unit = {},
    onDeletePartnerClick: (String) -> Unit = {},
    showMaxPartnerSnackbar: Boolean = false,
    onMaxPartnerSnackbarDismiss: () -> Unit = {},
) {
    if (showMaxPartnerSnackbar) {
        val currentOnMaxPartnerSnackbarDismiss by rememberUpdatedState(onMaxPartnerSnackbarDismiss)
        LaunchedEffect(showMaxPartnerSnackbar) {
            delay(PartnerSajuManagementDefaults.MAX_PARTNER_SNACKBAR_DURATION_MILLIS.milliseconds)
            currentOnMaxPartnerSnackbarDismiss()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TodakunSubHeader(
                title = stringResource(R.string.saju_info_management_title),
                onBackClick = onBackClick,
            )

            when (uiState) {
                is PartnerSajuManagementUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxSize())
                }

                is PartnerSajuManagementUiState.Fail -> {
                    Box(modifier = Modifier.weight(1f).fillMaxSize())
                }

                is PartnerSajuManagementUiState.Success -> {
                    PartnerSajuManagementContent(
                        model = uiState.model,
                        onAddPartnerClick = onAddPartnerClick,
                        onEditPartnerClick = onEditPartnerClick,
                        onDeletePartnerClick = onDeletePartnerClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (showMaxPartnerSnackbar) {
            TodakunSnackbar(
                text = stringResource(R.string.saju_info_management_max_partner_message),
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
            )
        }
    }
}

private object PartnerSajuManagementDefaults {
    const val MAX_PARTNER_SNACKBAR_DURATION_MILLIS = 2000L
}

@Composable
private fun PartnerSajuManagementContent(
    model: PartnerSajuManagementUiModel,
    onAddPartnerClick: () -> Unit,
    onEditPartnerClick: (String) -> Unit,
    onDeletePartnerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.saju_info_management_registered_count_label),
                style = TodakunTypography.body1Bold,
                color = TodakunColor.gray975,
            )
            Spacer(modifier = Modifier.width(8.dp))
            CountPill(count = model.partners.size)
        }

        Spacer(modifier = Modifier.height(16.dp))

        var expandedMenuLinkId by remember { mutableStateOf<String?>(null) }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            model.partners.forEach { partner ->
                PartnerSajuCard(
                    partner = partner,
                    menuExpanded = expandedMenuLinkId == partner.linkId,
                    onMoreClick = {
                        expandedMenuLinkId = if (expandedMenuLinkId == partner.linkId) null else partner.linkId
                    },
                    onMenuDismissRequest = { expandedMenuLinkId = null },
                    onEditClick = {
                        expandedMenuLinkId = null
                        onEditPartnerClick(partner.linkId)
                    },
                    onDeleteClick = {
                        expandedMenuLinkId = null
                        onDeletePartnerClick(partner.linkId)
                    },
                )
            }
            AddPartnerButton(onClick = onAddPartnerClick)
        }
    }
}

@Composable
private fun CountPill(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(TodakunColor.coolGray300),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = TodakunTypography.body3Medium,
            color = TodakunColor.coolGray600,
        )
    }
}

@Composable
private fun PartnerSajuCard(
    partner: PartnerSaju,
    menuExpanded: Boolean,
    onMoreClick: () -> Unit,
    onMenuDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, TodakunColor.gray200, RoundedCornerShape(12.dp))
                .background(TodakunColor.white)
                .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = partner.name, style = TodakunTypography.body1Bold, color = TodakunColor.gray975)
                Text(
                    text = stringResource(R.string.separator_dot),
                    style = TodakunTypography.body2Regular,
                    color = TodakunColor.coolGray700,
                )
                Text(
                    text = partner.gender.displayName(),
                    style = TodakunTypography.body2Regular,
                    color = TodakunColor.coolGray700,
                )
                TodakunBadge(text = partner.relationshipType.label, type = TodakunBadgeType.Gray)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = partner.birth.dateLabel(),
                    style = TodakunTypography.body3Medium,
                    color = TodakunColor.coolGray700,
                )
                partner.birth.timeLabel()?.let { timeText ->
                    Text(
                        text = stringResource(R.string.separator_dot),
                        style = TodakunTypography.body3Medium,
                        color = TodakunColor.coolGray700,
                    )
                    Text(text = timeText, style = TodakunTypography.body3Medium, color = TodakunColor.coolGray700)
                }
            }
        }

        Box {
            Icon(
                painter = painterResource(id = R.drawable.ic_more),
                contentDescription = stringResource(R.string.saju_info_management_more_content_description),
                tint = TodakunColor.gray400,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clickable { onMoreClick() },
            )
            val editMenuLabel = stringResource(R.string.saju_info_management_edit_menu)
            val deleteMenuLabel = stringResource(R.string.saju_info_management_delete_menu)
            TodakunPopover(
                contents = listOf(editMenuLabel, deleteMenuLabel),
                expanded = menuExpanded,
                onContentClick = { content ->
                    when (content) {
                        editMenuLabel -> onEditClick()
                        deleteMenuLabel -> onDeleteClick()
                    }
                },
                onDismissRequest = onMenuDismissRequest,
            )
        }
    }
}

@Composable
private fun AddPartnerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TodakunColor.coolGray50)
                .clickable(onClick = onClick)
                .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TodakunColor.blackOpacity05),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = null,
                tint = TodakunColor.blackOpacity30,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.saju_info_management_add_button),
            style = TodakunTypography.caption1Regular,
            color = TodakunColor.gray975,
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
private fun DateType.displayName(): String =
    when (this) {
        DateType.SOLAR -> stringResource(R.string.date_type_solar)
        DateType.LUNAR -> stringResource(R.string.date_type_lunar)
    }

@Composable
private fun Birth.dateLabel(): String =
    stringResource(
        R.string.mansaeryeok_profile_birth_date_only_format,
        date.format(BirthDateFormatter),
        dateType.displayName(),
    )

@Composable
private fun Birth.timeLabel(): String? {
    val start = time.startTime ?: return null
    val end = time.endTime ?: return null
    val range =
        stringResource(
            R.string.mansaeryeok_time_range_format,
            stringResource(R.string.mansaeryeok_time_colon_format, start.hour, start.minute),
            stringResource(
                R.string.mansaeryeok_time_colon_format,
                end.minusMinutes(1).hour,
                end.minusMinutes(1).minute,
            ),
        )
    return stringResource(R.string.saju_info_management_birth_time_format, range, time.displayName)
}

private val BirthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

@Preview(showBackground = true)
@Composable
private fun PartnerSajuManagementScreenPreview() {
    TodakunTheme {
        PartnerSajuManagementScreen(
            uiState =
                PartnerSajuManagementUiState.Success(
                    PartnerSajuManagementUiModel(
                        partners =
                            listOf(
                                PartnerSaju(
                                    linkId = "1",
                                    name = "토실이",
                                    gender = Gender.MALE,
                                    relationshipType = RelationshipType(code = "LOVER", label = "연인"),
                                    birth =
                                        Birth(
                                            dateType = DateType.SOLAR,
                                            date = LocalDate.of(1999, 2, 13),
                                            time = BirthTime.O,
                                        ),
                                ),
                                PartnerSaju(
                                    linkId = "2",
                                    name = "토실이",
                                    gender = Gender.MALE,
                                    relationshipType = RelationshipType(code = "COLLEAGUE", label = "동료"),
                                    birth =
                                        Birth(
                                            dateType = DateType.SOLAR,
                                            date = LocalDate.of(1999, 2, 13),
                                            time = BirthTime.O,
                                        ),
                                ),
                                PartnerSaju(
                                    linkId = "3",
                                    name = "토실이",
                                    gender = Gender.MALE,
                                    relationshipType = RelationshipType(code = "FRIEND", label = "친구"),
                                    birth =
                                        Birth(
                                            dateType = DateType.SOLAR,
                                            date = LocalDate.of(1999, 2, 13),
                                            time = BirthTime.O,
                                        ),
                                ),
                            ),
                    ),
                ),
        )
    }
}
