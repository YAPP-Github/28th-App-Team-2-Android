package com.kikidan.mypage.edit

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.User
import com.kikidan.domain.usecase.user.GetUserUseCase
import com.kikidan.domain.usecase.user.UpdateUserUseCase
import com.kikidan.mypage.edit.model.LifeStatus
import com.kikidan.mypage.edit.model.MyPageEditSideEffect
import com.kikidan.mypage.edit.model.MyPageEditUiModel
import com.kikidan.mypage.edit.model.MyPageEditUiState
import com.kikidan.mypage.edit.model.RelationshipStatus
import com.kikidan.mypage.edit.model.toDomain
import com.kikidan.mypage.edit.model.toJob
import com.kikidan.mypage.edit.model.toLifeStatus
import com.kikidan.mypage.edit.model.toUiStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

        init {
            container.stateFlow
                .onEach {
                    Log.e("moony", "heyyy: $it")
                }.launchIn(viewModelScope)
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
                                    lifeStatus = user.job.toLifeStatus(),
                                    relationshipStatus = user.relationshipStatus.toUiStatus(),
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

        fun updateCurrentSituation(
            lifeStatus: LifeStatus,
            relationshipStatus: RelationshipStatus,
        ) = updateModel { copy(lifeStatus = lifeStatus, relationshipStatus = relationshipStatus) }

        fun save() =
            intent {
                val currentState = state as? MyPageEditUiState.Success ?: return@intent
                if (currentState.isSaving) return@intent
                val model = currentState.model
                val user =
                    User(
                        id = model.id,
                        name = model.name,
                        gender = model.gender,
                        job = model.lifeStatus.toJob(),
                        relationshipStatus = model.relationshipStatus.toDomain(),
                        birth =
                            Birth(
                                dateType = model.dateType,
                                date = model.birthDate,
                                time = model.birthTime,
                            ),
                    )
                reduce { currentState.copy(isSaving = true) }
                updateUserUseCase(user)
                    .onSuccess {
                        postSideEffect(MyPageEditSideEffect.NavigateBack)
                    }.onFailure {
                        reduce { currentState.copy(isSaving = false) }
                    }
            }

        private fun updateModel(transform: MyPageEditUiModel.() -> MyPageEditUiModel) =
            intent {
                val currentState = state as? MyPageEditUiState.Success ?: return@intent
                reduce { currentState.copy(model = currentState.model.transform()) }
            }
    }
