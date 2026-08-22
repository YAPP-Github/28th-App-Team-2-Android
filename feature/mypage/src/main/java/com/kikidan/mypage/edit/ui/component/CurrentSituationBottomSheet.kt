package com.kikidan.mypage.edit.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.component.TodakunChip
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.mypage.edit.model.LifeStatus
import com.kikidan.mypage.edit.model.RelationshipStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentSituationBottomSheet(
    onSaveClick: (LifeStatus, RelationshipStatus) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialLifeStatus: LifeStatus? = null,
    initialRelationshipStatus: RelationshipStatus? = null,
) {
    var lifeStatus by remember { mutableStateOf(initialLifeStatus) }
    var relationshipStatus by remember { mutableStateOf(initialRelationshipStatus) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TodakunColor.white,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = stringResource(R.string.header_close_content_description),
                    tint = TodakunColor.gray500,
                    modifier =
                        Modifier
                            .align(Alignment.CenterEnd)
                            .size(24.dp)
                            .clickable(onClick = onDismissRequest),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.mypage_current_situation_title),
                style = TodakunTypography.heading4Bold,
                color = TodakunColor.gray975,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(52.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.mypage_current_situation_life_label),
                        style = TodakunTypography.body1Bold,
                        color = TodakunColor.black,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        LifeStatus.entries.forEach { status ->
                            TodakunChip(
                                label = stringResource(status.labelRes),
                                selected = status == lifeStatus,
                                onClick = { lifeStatus = status },
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.mypage_current_situation_relationship_label),
                        style = TodakunTypography.body1Bold,
                        color = TodakunColor.black,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RelationshipStatus.entries.forEach { status ->
                            TodakunChip(
                                label = stringResource(status.labelRes),
                                selected = status == relationshipStatus,
                                onClick = { relationshipStatus = status },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = stringResource(R.string.mypage_edit_save_button),
                onClick = {
                    val life = lifeStatus
                    val relationship = relationshipStatus
                    if (life != null && relationship != null) {
                        onSaveClick(life, relationship)
                    }
                },
                enabled = lifeStatus != null && relationshipStatus != null,
                size = TodakunButtonSize.Large,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentSituationBottomSheetPreview() {
    TodakunTheme {
        CurrentSituationBottomSheet(
            onSaveClick = { _, _ -> },
            onDismissRequest = {},
            initialLifeStatus = LifeStatus.OFFICE_WORKER,
            initialRelationshipStatus = RelationshipStatus.SINGLE,
        )
    }
}
