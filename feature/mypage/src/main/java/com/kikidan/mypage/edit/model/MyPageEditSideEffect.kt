package com.kikidan.mypage.edit.model

sealed interface MyPageEditSideEffect {
    data object NavigateBack : MyPageEditSideEffect
}
