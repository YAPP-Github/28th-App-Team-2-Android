package com.kikidan.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kikidan.designsystem.component.wheelpicker.BirthDateState
import com.kikidan.designsystem.component.wheelpicker.BirthDateWheelPicker
import com.kikidan.designsystem.component.wheelpicker.SajuBirthTimeWheelPicker
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.onboarding.model.OnboardingSheet
import com.kikidan.onboarding.model.OnboardingSideEffect
import com.kikidan.onboarding.model.OnboardingStep
import com.kikidan.onboarding.screen.BirthInfoScreen
import com.kikidan.onboarding.screen.CompleteScreen
import com.kikidan.onboarding.screen.ExtraQuestionScreen
import com.kikidan.onboarding.screen.NameScreen
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import java.time.LocalDate

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    onNavigateTerm: () -> Unit,
    onboardingToken: OnboardingToken,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.collectAsState()
    var permissionHandled by rememberSaveable {
        mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) {
            // 권한을 허락하지 않더라도 앱 진입
            permissionHandled = true
            onFinish()
        }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            OnboardingSideEffect.NavigateToHome -> {
                onFinish()
            }

            OnboardingSideEffect.NavigateToTerms -> {
                onNavigateTerm()
            }

            OnboardingSideEffect.PermissionRequest -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    onFinish()
                }
            }

            else -> { /* TODO 에러 처리 스낵바 또는 다이얼로그 */ }
        }
    }

    BackHandler {
        viewModel.clickBack()
    }

    when (state.step) {
        OnboardingStep.NAME -> {
            NameScreen(
                username = state.username,
                canProceed = state.canProceed,
                onNameChange = viewModel::changeName,
                onNextClick = viewModel::clickNext,
                onBackClick = viewModel::clickBack,
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
                isBirthDateError = state.isUnderAge,
                onGenderSelect = viewModel::selectGender,
                onCalendarTypeSelect = viewModel::selectCalendarType,
                onSheetOpen = viewModel::openSheet,
                onBirthDateClear = viewModel::clearBirthDate,
                onBirthTimeClear = viewModel::clearBirthTime,
                onBirthTimeUnknownChange = viewModel::changeBirthTimeUnknown,
                onNextClick = viewModel::clickNext,
                onBackClick = viewModel::clickBack,
                modifier = modifier,
            )
        }

        OnboardingStep.EXTRA_QUESTION -> {
            ExtraQuestionScreen(
                lifeStage = state.lifeStage,
                relationshipStatus = state.relationshipStatus,
                canProceed = state.canProceed,
                onLifeStageSelect = viewModel::selectLifeStage,
                onRelationshipStatusSelect = viewModel::selectRelationshipStatus,
                onNextClick = {
                    viewModel.confirmComplete(onboardingToken)
                },
                onBackClick = viewModel::clickBack,
                modifier = modifier,
            )
        }

        OnboardingStep.COMPLETE -> {
            if (permissionHandled) {
                CompleteScreen(
                    modifier = modifier,
                )
            }
        }
    }

    OnboardingSheetHost(
        sheet = state.sheet,
        birthDate = state.birthDate,
        onBirthDateChange = viewModel::changeBirthDate,
        onBirthTimeChange = viewModel::changeBirthTime,
        onDismiss = viewModel::dismissSheet,
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
        OnboardingSheet.BIRTH_DATE -> {
            // 휠을 굴리는 동안의 값은 시트 안에서만 유효하고, 저장할 때 비로소 상태에 반영한다.
            var draft by remember(birthDate) {
                mutableStateOf(
                    BirthDateState.of(
                        birthDate ?: OnboardingRouteDefaults.DefaultBirthDate,
                        OnboardingRouteDefaults.BirthDateYearRange,
                    ),
                )
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
                    (draft ?: OnboardingRouteDefaults.DefaultBirthTime).let(onBirthTimeChange)
                    onDismiss()
                },
                onDismissRequest = onDismiss,
            )
        }

        null -> {
            Unit
        }
    }
}

private object OnboardingRouteDefaults {
    val DefaultBirthDate: LocalDate = LocalDate.of(1999, 2, 13)
    val BirthDateYearRange = 1900..2025

    /** 휠피커가 처음 보여주는 항목(index 2 = 인시). 사용자가 굴리지 않고 저장하면 이 값이 선택된 것으로 본다. */
    val DefaultBirthTime = BirthTime.IN
}

/**
 * `"자시 (子時): 23:30 ~ 01:29"` 형태의 휠피커 label을 지시로 되돌린다.
 * 시각 범위 표기가 바뀌어도 앞머리 지지 이름만 유지되면 계속 동작한다.
 */
private fun birthTimeFromLabel(label: String): BirthTime? {
    val trimmed = label.trimStart()
    return BirthTime.entries.firstOrNull { trimmed.startsWith(it.displayName) }
}
