package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.CreateDayFortunesUseCase
import com.kikidan.domain.usecase.DateFortuneDefaults
import com.kikidan.sajucontents.R
import com.kikidan.sajucontents.model.DateFortuneSideEffect
import com.kikidan.sajucontents.model.DateFortuneState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DateFortuneViewModel
    @Inject
    constructor(
        private val createDayFortunes: CreateDayFortunesUseCase,
    ) : ViewModel(),
        ContainerHost<DateFortuneState, DateFortuneSideEffect> {
        // orbit-viewmodel의 `ViewModel.container(...)` 팩토리는 Container를 반환하므로
        // ContainerHost.container 프로퍼티를 override하는 형태로 위임한다.
        override val container: Container<DateFortuneState, DateFortuneSideEffect> =
            container(DateFortuneState())

        fun onPurposeSelect(purpose: DayFortunePurpose) =
            intent {
                reduce { state.copy(selectedPurpose = purpose) }
            }

        fun onGenderSelect(gender: Gender) =
            intent {
                reduce { state.copy(selectedGender = gender) }
            }

        fun onOpenDateSheet() =
            intent {
                reduce { state.copy(isSheetVisible = true) }
            }

        fun onCloseDateSheet() =
            intent {
                reduce { state.copy(isSheetVisible = false) }
            }

        fun onDateToggle(date: LocalDate) =
            intent {
                val current = state.selectedDates
                when {
                    current.contains(date) -> {
                        reduce { state.copy(selectedDates = current.filterNot { it == date }.toPersistentList()) }
                    }

                    current.size >= DateFortuneDefaults.MAX_TARGET_DATES -> {
                        postSideEffect(DateFortuneSideEffect.ShowToast(R.string.date_fortune_max_dates_toast))
                    }

                    else -> {
                        reduce { state.copy(selectedDates = (current + date).toPersistentList()) }
                    }
                }
            }

        fun onDateRemove(date: LocalDate) =
            intent {
                reduce { state.copy(selectedDates = state.selectedDates.filterNot { it == date }.toPersistentList()) }
            }

        fun onReset() =
            intent {
                reduce { state.copy(selectedDates = persistentListOf()) }
            }

        fun onResultTabSelect(index: Int) =
            intent {
                reduce { state.copy(selectedResultIndex = index) }
            }

        fun onSubmit() =
            intent {
                val purpose = state.selectedPurpose ?: return@intent
                val dates = state.selectedDates
                if (dates.isEmpty() || state.isLoading) return@intent

                reduce { state.copy(isLoading = true) }

                // ponytail: gender는 서버 Request 필드 추가 후 전달 (블로커 B-2)
                createDayFortunes(purpose, dates).fold(
                    onSuccess = { results ->
                        reduce {
                            state.copy(
                                isLoading = false,
                                results = results.toPersistentList(),
                                selectedResultIndex = 0,
                            )
                        }
                        postSideEffect(DateFortuneSideEffect.NavigateToResult)
                    },
                    onFailure = {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(DateFortuneSideEffect.ShowError(R.string.date_fortune_submit_error))
                    },
                )
            }
    }
