package com.kikidan.mypage.partner

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.saju.DeletePartnerSajuUseCase
import com.kikidan.domain.usecase.saju.GetPartnerSajuListUseCase
import com.kikidan.mypage.partner.model.PartnerSajuManagementSideEffect
import com.kikidan.mypage.partner.model.PartnerSajuManagementUiModel
import com.kikidan.mypage.partner.model.PartnerSajuManagementUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class PartnerSajuManagementViewModel
    @Inject
    constructor(
        private val getPartnerSajuListUseCase: GetPartnerSajuListUseCase,
        private val deletePartnerSajuUseCase: DeletePartnerSajuUseCase,
    ) : ViewModel(),
        ContainerHost<PartnerSajuManagementUiState, PartnerSajuManagementSideEffect> {
        override val container: Container<PartnerSajuManagementUiState, PartnerSajuManagementSideEffect> =
            container(PartnerSajuManagementUiState.Loading) {
                loadPartnerSajuList()
            }

        fun loadPartnerSajuList() =
            intent {
                getPartnerSajuListUseCase()
                    .onSuccess { partners ->
                        reduce { PartnerSajuManagementUiState.Success(PartnerSajuManagementUiModel(partners)) }
                    }.onFailure { throwable ->
                        reduce { PartnerSajuManagementUiState.Fail(throwable) }
                    }
            }

        fun deletePartner(linkId: String) =
            intent {
                val currentState = state as? PartnerSajuManagementUiState.Success ?: return@intent
                val updated = currentState.model.partners.filterNot { it.linkId == linkId }
                reduce { currentState.copy(model = currentState.model.copy(partners = updated)) }
                deletePartnerSajuUseCase(linkId)
            }
    }
