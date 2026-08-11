package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.domain.model.dayfortune.DayFortune
import kotlinx.collections.immutable.ImmutableList
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TabDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M.d(E)", Locale.KOREAN)

/** 결과 화면 상단 날짜 탭. 개수는 [results]에 따라 동적(1~5개, D7 상한 상수 참조). */
@Composable
fun ResultDateTabRow(
    results: ImmutableList<DayFortune>,
    selectedIndex: Int,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(54.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        results.forEachIndexed { index, fortune ->
            val selected = index == selectedIndex
            val label = remember(fortune.targetDate) { fortune.targetDate.format(TabDateFormatter) }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) TodakunColor.primary600 else TodakunColor.white.copy(alpha = 0.05f))
                        .border(
                            width = 1.dp,
                            color = if (selected) TodakunColor.primary500 else TodakunColor.white.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp),
                        ).selectable(selected = selected, role = Role.Tab, onClick = { onTabSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = TodakunTypography.body2Medium,
                    color = TodakunColor.white,
                )
            }
        }
    }
}
