package com.kikidan.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.component.dialog.TodakunDialog
import com.kikidan.designsystem.component.wheelpicker.BirthDateState
import com.kikidan.designsystem.component.wheelpicker.BirthDateWheelPicker
import com.kikidan.designsystem.component.wheelpicker.SajuBirthTimeWheelPicker
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.onboarding.screen.BirthInfoScreen
import com.kikidan.onboarding.screen.ExtraQuestionScreen
import com.kikidan.onboarding.screen.NameScreen
import com.kikidan.onboarding.screen.TermsScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    onExit: () -> Unit,
    onboardingToken: OnboardingToken,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            OnboardingSideEffect.NavigateToHome -> {
                onFinish()
            }

            OnboardingSideEffect.Exit -> {
                onExit()
            }

            else -> { /* TODO 에러 처리 스낵바 또는 다이얼로그 */ }
        }
    }

    BackHandler {
        viewModel.onBackClick()
    }

    when (state.step) {
        OnboardingStep.TERMS -> {
            TermsScreen(
                termsAgreement = state.termsAgreement,
                canProceed = state.canProceed,
                onTermChange = viewModel::onTermChange,
                onAllTermsChange = viewModel::onAllTermsChange,
                onNextClick = viewModel::onNextClick,
                onBackClick = viewModel::onBackClick,
                modifier = modifier,
            )
        }

        OnboardingStep.NAME -> {
            NameScreen(
                username = state.username,
                canProceed = state.canProceed,
                onNameChange = viewModel::onNameChange,
                onNextClick = viewModel::onNextClick,
                onBackClick = viewModel::onBackClick,
                modifier = modifier,
            )
        }

        OnboardingStep.BIRTH_INFO -> {
            BirthInfoScreen(
                gender = state.gender,
                calendarType = state.calendarType,
                birthDate = state.birthDate,
                birthTime = state.birthTime,
                openedSheet = state.sheet,
                canProceed = state.canProceed,
                onGenderSelect = viewModel::onGenderSelect,
                onCalendarTypeSelect = viewModel::onCalendarTypeSelect,
                onSheetOpen = viewModel::onSheetOpen,
                onBirthDateClear = viewModel::onBirthDateClear,
                onBirthTimeClear = viewModel::onBirthTimeClear,
                onBirthTimeUnknownChange = viewModel::onBirthTimeUnknownChange,
                onNextClick = viewModel::onNextClick,
                onBackClick = viewModel::onBackClick,
                modifier = modifier,
            )
        }

        OnboardingStep.EXTRA_QUESTION -> {
            ExtraQuestionScreen(
                lifeStage = state.lifeStage,
                relationshipStatus = state.relationshipStatus,
                canProceed = state.canProceed,
                onLifeStageSelect = viewModel::onLifeStageSelect,
                onRelationshipStatusSelect = viewModel::onRelationshipStatusSelect,
                onNextClick = {
                    viewModel.onCompleteConfirmed(onboardingToken)
                },
                onBackClick = viewModel::onBackClick,
                modifier = modifier,
            )
        }
    }

    OnboardingSheetHost(
        sheet = state.sheet,
        birthDate = state.birthDate,
        onBirthDateChange = viewModel::onBirthDateChange,
        onBirthTimeChange = viewModel::onBirthTimeChange,
        onDismiss = viewModel::onSheetDismiss,
    )

    OnboardingDialogHost(
        dialog = state.dialog,
        onConfirmExit = viewModel::onExitConfirmed,
        onDismiss = viewModel::onDialogDismiss,
    )
}

@Composable
private fun OnboardingSheetHost(
    sheet: OnboardingSheet?,
    birthDate: LocalDate?,
    onBirthDateChange: (LocalDate) -> Unit,
    onBirthTimeChange: (BirthTime) -> Unit,
    onDismiss: () -> Unit,
) {
    when (sheet) {
        null -> {
            Unit
        }

        OnboardingSheet.BIRTH_DATE -> {
            // 휠을 굴리는 동안의 값은 시트 안에서만 유효하고, 저장할 때 비로소 상태에 반영한다.
            var draft by remember(birthDate) {
                mutableStateOf(BirthDateState.of(birthDate ?: DefaultBirthDate, BirthDateYearRange))
            }
            BirthDateWheelPicker(
                birthDateState = draft,
                onBirthDateChange = { draft = it },
                onSaveClick = {
                    onBirthDateChange(LocalDate.of(draft.year, draft.month, draft.day))
                    onDismiss()
                },
                onDismissRequest = onDismiss,
            )
        }

        OnboardingSheet.BIRTH_TIME -> {
            // SajuBirthTimeWheelPicker는 label 문자열만 돌려주므로 표시 이름으로 역매핑한다.
            var draft by remember { mutableStateOf<BirthTime?>(null) }
            SajuBirthTimeWheelPicker(
                onSajuBirthTimeChange = { label -> draft = birthTimeFromLabel(label) },
                onSaveClick = {
                    (draft ?: DefaultBirthTime).let(onBirthTimeChange)
                    onDismiss()
                },
                onDismissRequest = onDismiss,
            )
        }
    }
}

@Composable
private fun OnboardingDialogHost(
    dialog: OnboardingDialog?,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        null -> {
            Unit
        }

        OnboardingDialog.EXIT_CONFIRM -> {
            TodakunDialog(
                title = stringResource(id = R.string.onboarding_terms_exit_title),
                description = stringResource(id = R.string.onboarding_terms_exit_description),
                confirmText = stringResource(id = R.string.onboarding_terms_exit_confirm),
                dismissText = stringResource(id = R.string.onboarding_terms_exit_dismiss),
                onConfirm = onConfirmExit,
                onDismiss = onDismiss,
            )
        }

        OnboardingDialog.SIGN_UP_COMPLETE -> {
            TodakunDialog(
                title = stringResource(id = R.string.onboarding_complete_title),
                description = stringResource(id = R.string.onboarding_complete_description),
                confirmText = stringResource(id = R.string.onboarding_confirm),
                onConfirm = { },
                onDismiss = { },
            )
        }
    }
}

private val DefaultBirthDate: LocalDate = LocalDate.of(1999, 2, 13)
private val BirthDateYearRange = 1900..2025

/** 휠피커가 처음 보여주는 항목(index 2 = 인시). 사용자가 굴리지 않고 저장하면 이 값이 선택된 것으로 본다. */
private val DefaultBirthTime = BirthTime.IN

/**
 * `"자시 (子時): 23:30 ~ 01:29"` 형태의 휠피커 label을 지시로 되돌린다.
 * 시각 범위 표기가 바뀌어도 앞머리 지지 이름만 유지되면 계속 동작한다.
 */
private fun birthTimeFromLabel(label: String): BirthTime? {
    val trimmed = label.trimStart()
    return BirthTime.entries.firstOrNull { trimmed.startsWith(it.displayName) }
}
