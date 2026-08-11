package com.kikidan.sajucontents.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.sajucontents.R
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.kikidan.designsystem.R as DesignSystemR

private val DateChipFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d(E)", Locale.KOREAN)

@Composable
fun SelectedDateChipRow(
    selectedDates: ImmutableList<LocalDate>,
    onDateRemove: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selectedDates.isEmpty()) return

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
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = TodakunTypography.body3Medium,
            color = TodakunColor.primary700,
        )
        Icon(
            painter = painterResource(id = DesignSystemR.drawable.ic_close),
            contentDescription = stringResource(id = R.string.date_fortune_date_remove),
            tint = TodakunColor.primary400,
            modifier = Modifier.size(16.dp).clickable(onClick = onRemove),
        )
    }
}
