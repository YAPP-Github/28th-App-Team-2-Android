package com.kikidan.mypage.partner.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.saju.GetPartnerSajuUseCase
import com.kikidan.domain.usecase.saju.RegisterPartnerSajuUseCase
import com.kikidan.domain.usecase.saju.UpdatePartnerSajuUseCase
import com.kikidan.mypage.partner.form.model.PartnerRelationshipType
import com.kikidan.mypage.partner.form.model.PartnerSajuFormSideEffect
import com.kikidan.mypage.partner.form.model.PartnerSajuFormUiModel
import com.kikidan.mypage.partner.form.model.PartnerSajuFormUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PartnerSajuFormViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val getPartnerSajuUseCase: GetPartnerSajuUseCase,
        private val registerPartnerSajuUseCase: RegisterPartnerSajuUseCase,
        private val updatePartnerSajuUseCase: UpdatePartnerSajuUseCase,
    ) : ViewModel(),
        ContainerHost<PartnerSajuFormUiState, PartnerSajuFormSideEffect> {
        private var linkId: String? = savedStateHandle[LINK_ID_KEY]

        override val container: Container<PartnerSajuFormUiState, PartnerSajuFormSideEffect> =
            container(PartnerSajuFormUiState.Loading) {
                loadForm()
            }

        /** 화면 진입 시 1회. Navigation3는 라우트 인자를 SavedStateHandle에 자동 주입하지 않아 명시적으로 전달받는다. */
        fun load(linkId: String?) =
            intent {
                this@PartnerSajuFormViewModel.linkId = linkId
                savedStateHandle[LINK_ID_KEY] = linkId
                loadForm()
            }

        private fun loadForm() =
            intent {
                val targetLinkId = linkId
                if (targetLinkId == null) {
                    reduce { PartnerSajuFormUiState.Success(NewPartnerSajuFormUiModel) }
                    return@intent
                }
                getPartnerSajuUseCase(targetLinkId)
                    .onSuccess { partner ->
                        reduce {
                            PartnerSajuFormUiState.Success(
                                PartnerSajuFormUiModel(
                                    linkId = partner.linkId,
                                    name = partner.name,
                                    gender = partner.gender,
                                    dateType = partner.birth.dateType,
                                    birthDate = partner.birth.date,
                                    birthTime = partner.birth.time,
                                    relationshipTypeCode = partner.relationshipType.code,
                                ),
                            )
                        }
                    }.onFailure { throwable ->
                        reduce { PartnerSajuFormUiState.Fail(throwable) }
                    }
            }

        fun updateName(name: String) = updateModel { it.copy(name = name) }

        fun selectGender(gender: Gender) = updateModel { it.copy(gender = gender) }

        fun selectDateType(dateType: DateType) = updateModel { it.copy(dateType = dateType) }

        fun updateBirthDate(birthDate: LocalDate?) = updateModel { it.copy(birthDate = birthDate) }

        fun updateBirthTime(birthTime: BirthTime) = updateModel { it.copy(birthTime = birthTime) }

        fun selectRelationshipType(code: String) = updateModel { it.copy(relationshipTypeCode = code) }

        fun save() =
            intent {
                val currentState = state as? PartnerSajuFormUiState.Success ?: return@intent
                val model = currentState.model
                val birthDate = model.birthDate ?: return@intent
                val input =
                    PartnerSajuInput(
                        name = model.name,
                        gender = model.gender,
                        relationshipTypeCode = model.relationshipTypeCode,
                        birth = Birth(dateType = model.dateType, date = birthDate, time = model.birthTime),
                    )
                val result =
                    if (model.linkId != null) {
                        updatePartnerSajuUseCase(model.linkId, input)
                    } else {
                        registerPartnerSajuUseCase(input)
                    }
                result.onSuccess {
                    postSideEffect(PartnerSajuFormSideEffect.NavigateBack)
                }
            }

        private fun updateModel(transform: (PartnerSajuFormUiModel) -> PartnerSajuFormUiModel) =
            intent {
                val currentState = state as? PartnerSajuFormUiState.Success ?: return@intent
                reduce { currentState.copy(model = transform(currentState.model)) }
            }

        companion object {
            private const val LINK_ID_KEY = "linkId"

            private val NewPartnerSajuFormUiModel =
                PartnerSajuFormUiModel(
                    linkId = null,
                    name = "",
                    gender = Gender.MALE,
                    dateType = DateType.SOLAR,
                    birthDate = null,
                    birthTime = BirthTime.UNKNOWN,
                    relationshipTypeCode = PartnerRelationshipType.LOVER.code,
                )
        }
    }
