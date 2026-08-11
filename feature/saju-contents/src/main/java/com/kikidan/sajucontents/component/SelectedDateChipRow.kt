package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateChipFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d(E)", Locale.KOREAN)

@Composable
internal fun SelectedDateChipRow(
    selectedDates: ImmutableList<LocalDate>,
    onDateRemove: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(selectedDates, key = { it.toString() }) { date ->
            DateChip(date = date, onRemove = { onDateRemove(date) })
        }
    }
}

@Composable
private fun DateChip(
    date: LocalDate,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = remember(date) { date.format(DateChipFormatter) }

    Row(
        modifier =
            modifier
                .border(1.dp, TodakunColor.primary300, RoundedCornerShape(99.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(onClick = onRemove),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = TodakunTypography.body2Medium,
            color = TodakunColor.primary700,
        )
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color = TodakunColor.gray300),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(13.dp),
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = stringResource(id = R.string.date_fortune_date_remove),
                tint = TodakunColor.white,
            )
        }
    }
}
