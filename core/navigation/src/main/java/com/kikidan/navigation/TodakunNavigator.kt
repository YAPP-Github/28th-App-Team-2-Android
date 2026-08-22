package com.kikidan.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kikidan.designsystem.component.bottomnavigation.TodakunNavItem

class TodakunNavigator(
    val backStack: NavBackStack<NavKey>,
) {
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
