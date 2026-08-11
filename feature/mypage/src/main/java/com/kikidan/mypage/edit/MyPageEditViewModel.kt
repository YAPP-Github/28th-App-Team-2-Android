package com.kikidan.mypage.edit

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.User
import com.kikidan.domain.usecase.user.GetUserUseCase
import com.kikidan.domain.usecase.user.UpdateUserUseCase
import com.kikidan.mypage.edit.model.MyPageEditSideEffect
import com.kikidan.mypage.edit.model.MyPageEditUiModel
import com.kikidan.mypage.edit.model.MyPageEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyPageEditViewModel
    @Inject
    constructor(
        private val getUserUseCase: GetUserUseCase,
        private val updateUserUseCase: UpdateUserUseCase,
    ) : ViewModel(),
        ContainerHost<MyPageEditUiState, MyPageEditSideEffect> {
        override val container: Container<MyPageEditUiState, MyPageEditSideEffect> =
            container(MyPageEditUiState.Loading) {
                loadUser()
            }

        fun loadUser() =
            intent {
                getUserUseCase()
                    .onSuccess { user ->
                        reduce {
                            MyPageEditUiState.Success(
                                MyPageEditUiModel(
                                    id = user.id,
                                    name = user.name,
                                    gender = user.gender,
                                    dateType = user.birth.dateType,
                                    birthDate = user.birth.date,
                                    birthTime = user.birth.time,
                                ),
                            )
                        }
                    }.onFailure { throwable ->
                        reduce { MyPageEditUiState.Fail(throwable) }
                    }
            }

        fun selectGender(gender: Gender) = updateModel { copy(gender = gender) }

        fun selectDateType(dateType: DateType) = updateModel { copy(dateType = dateType) }

        fun updateBirthDate(date: LocalDate) = updateModel { copy(birthDate = date) }

        fun updateBirthTime(birthTime: BirthTime) = updateModel { copy(birthTime = birthTime) }

        fun save() =
            intent {
                val currentState = state as? MyPageEditUiState.Success ?: return@intent
                val model = currentState.model
                val user =
                    User(
                        id = model.id,
                        name = model.name,
                        gender = model.gender,
                        birth =
                            Birth(
                                dateType = model.dateType,
                                date = model.birthDate,
                                time = model.birthTime,
                            ),
                    )
                updateUserUseCase(user).onSuccess {
                    postSideEffect(MyPageEditSideEffect.NavigateBack)
                }
            }

        private fun updateModel(transform: MyPageEditUiModel.() -> MyPageEditUiModel) =
            intent {
                val currentState = state as? MyPageEditUiState.Success ?: return@intent
                reduce { currentState.copy(model = currentState.model.transform()) }
            }
    }
