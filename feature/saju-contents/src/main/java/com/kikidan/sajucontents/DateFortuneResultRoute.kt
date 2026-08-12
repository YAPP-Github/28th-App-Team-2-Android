package com.kikidan.sajucontents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.R
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.sajucontents.model.DateFortuneResultSideEffect
import com.kikidan.sajucontents.screen.DateFortuneResultScreen
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.ZoneOffset

@Composable
fun DateFortuneResultRoute(
    ids: List<String>,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onAskTodakClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DateFortuneResultViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val loadErrorMessage = stringResource(R.string.date_fortune_result_load_error)
    val shareErrorMessage = stringResource(R.string.date_fortune_share_kakao_error)
    val urlCopiedMessage = stringResource(R.string.date_fortune_share_url_copied)
    val calendarErrorMessage = stringResource(R.string.date_fortune_export_calendar_error)

    LaunchedEffect(ids) {
        viewModel.loadResults(ids)
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            DateFortuneResultSideEffect.ShowError -> {
                snackbarHostState.showSnackbar(loadErrorMessage)
            }

            DateFortuneResultSideEffect.ShowShareError -> {
                snackbarHostState.showSnackbar(shareErrorMessage)
            }

            DateFortuneResultSideEffect.ShowUrlCopied -> {
                snackbarHostState.showSnackbar(urlCopiedMessage)
            }
        }
    }

    DateFortuneResultScreen(
        state = state,
        onBackClick = onNavigateBack,
        onTabSelect = viewModel::selectTabResult,
        onShareClick = viewModel::showShareDialog,
        onShareDismiss = viewModel::hideShareDialog,
        onKakaoShareFail = viewModel::notifyShareUnavailable,
        onUrlCopy = viewModel::notifyUrlCopied,
        onExportClick = {
            val fortune = state.results.getOrNull(state.selectedResultIndex)
            if (fortune != null) {
                try {
                    context.startActivity(fortune.toCalendarInsertIntent())
                } catch (e: ActivityNotFoundException) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(calendarErrorMessage) }
                }
            }
        },
        onAskTodakClick = onAskTodakClick,
        modifier = modifier,
    )
}

private fun DayFortune.toCalendarInsertIntent(): Intent {
    val beginMillis = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val endMillis =
        targetDate
            .plusDays(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    return Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.Events.DESCRIPTION, content)
    }
}
