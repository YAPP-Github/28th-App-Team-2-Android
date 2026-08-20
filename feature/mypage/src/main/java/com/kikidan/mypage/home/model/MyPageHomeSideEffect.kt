package com.kikidan.mypage.home.model

sealed interface MyPageHomeSideEffect {
    data object NavigateToLogin : MyPageHomeSideEffect

    data object ShowLogoutError : MyPageHomeSideEffect
}
