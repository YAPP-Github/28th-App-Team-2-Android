package com.kikidan.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kikidan.designsystem.component.bottomnavigation.TodakunNavItem

/**
 * [TodakunRoute] 백스택과 인증/딥링크 진입 정책을 캡슐화한다.
 * `isAuthenticated`는 별도 플래그 대신 백스택의 루트 라우트로부터 유도한다 —
 * 백스택 자체가 이미 프로세스 재생성에도 살아남는 상태이므로 이중 관리를 피한다.
 */
class TodakunNavigator(
    val backStack: NavBackStack<NavKey>,
) {
    private var pendingDeepLink: String? = null

    val isAuthenticated: Boolean
        get() = backStack.firstOrNull().let { it != TodakunRoute.Login && it != TodakunRoute.Terms }

    val currentTab: TodakunNavItem?
        get() = (backStack.lastOrNull() as? TodakunRoute)?.toNavItemOrNull()

    fun resetTo(route: TodakunRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun push(route: TodakunRoute) {
        backStack.add(route)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    fun selectTab(item: TodakunNavItem) {
        val route = item.toRoute()
        resetTo(TodakunRoute.Home)
        push(route)
    }

    fun navigateOrFallbackToDeepLink(deepLink: String) {
        if (!isAuthenticated) {
            pendingDeepLink = deepLink
            return
        }
        parseDeepLink(deepLink)?.let(::push) ?: push(TodakunRoute.Notification)
    }
}

private fun TodakunRoute.toNavItemOrNull(): TodakunNavItem? =
    when (this) {
        TodakunRoute.Home -> TodakunNavItem.FORTUNE_TELLING
        TodakunRoute.LuckAction -> TodakunNavItem.LUCKY_ACTION
        TodakunRoute.MyPage -> TodakunNavItem.MY
        else -> null
    }

private fun TodakunNavItem.toRoute(): TodakunRoute =
    when (this) {
        TodakunNavItem.FORTUNE_TELLING -> TodakunRoute.Home
        TodakunNavItem.TODAK_CHAT -> TodakunRoute.Chat()
        TodakunNavItem.LUCKY_ACTION -> TodakunRoute.LuckAction
        TodakunNavItem.MY -> TodakunRoute.MyPage
    }
