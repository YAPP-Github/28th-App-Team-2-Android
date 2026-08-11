package com.kikidan.sajucontents.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.SecondaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.designsystem.util.noRippleClickable
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectBottomSheet(
    selectedDates: ImmutableList<LocalDate>,
    onDateToggle: (LocalDate) -> Unit,
    onDateRemove: (LocalDate) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = TodakunColor.white,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TodakunColor.gray200) },
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DateSelectBottomSheetTitle(
                onDismissClick = {
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            MultiSelectCalendar(
                selectedDates = selectedDates,
                onDateToggle = onDateToggle,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
            )
            SelectedDateChipRow(
                selectedDates = selectedDates,
                onDateRemove = onDateRemove,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryButton(
                    text = stringResource(id = R.string.date_fortune_reset),
                    onClick = onReset,
                    size = TodakunButtonSize.Large,
                )
                PrimaryButton(
                    text = stringResource(id = R.string.date_fortune_confirm),
                    onClick = onConfirm,
                    enabled = selectedDates.isNotEmpty(),
                    size = TodakunButtonSize.Large,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DateSelectBottomSheetTitle(
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.date_fortune_sheet_title),
                style = TodakunTypography.heading3Bold,
                color = TodakunColor.gray975,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    stringResource(
                        id = R.string.date_fortune_sheet_subtitle,
                        DateFortuneDefaults.MAX_TARGET_DATES,
                    ),
                style = TodakunTypography.body3Regular,
                color = TodakunColor.gray700,
            )
        }

        Icon(
            modifier = Modifier.size(24.dp).noRippleClickable(onClick = onDismissClick),
            tint = TodakunColor.gray925,
            painter = painterResource(com.kikidan.designsystem.R.drawable.ic_close),
            contentDescription = stringResource(R.string.date_fortune_close_content_description),
        )
    }
}
