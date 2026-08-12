package com.kikidan.sajucontents.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.component.TodakunSelectBox
import com.kikidan.designsystem.component.button.PrimaryButton
import com.kikidan.designsystem.component.button.TodakunButtonSize
import com.kikidan.designsystem.component.header.TodakunSubHeader
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTheme
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.model.YearFortuneState

@Composable
internal fun YearSelectionScreen(
    state: YearFortuneState,
    onBackClick: () -> Unit,
    onYearSelect: (Int) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(TodakunColor.white),
    ) {
        TodakunSubHeader(
            title = stringResource(id = R.string.year_fortune_header_title),
            onBackClick = onBackClick,
        )
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(id = R.string.year_fortune_select_title),
                    style = TodakunTypography.heading3Bold,
                    color = TodakunColor.gray975,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                )
                state.years.chunked(2).forEach { rowYears ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowYears.forEach { year ->
                            TodakunSelectBox(
                                text =
                                    if (year == state.currentYear) {
                                        stringResource(id = R.string.year_fortune_year_this_year, year)
                                    } else {
                                        year.toString()
                                    },
                                selected = year == state.selectedYear,
                                onClick = { onYearSelect(year) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
        PrimaryButton(
            text = stringResource(id = R.string.year_fortune_cta),
            onClick = onSubmit,
            size = TodakunButtonSize.Large,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YearSelectionScreenPreview() {
    TodakunTheme {
        YearSelectionScreen(
            state = YearFortuneState(selectedYear = 2026, currentYear = 2026),
            onBackClick = {},
            onYearSelect = {},
            onSubmit = {},
        )
    }
}
