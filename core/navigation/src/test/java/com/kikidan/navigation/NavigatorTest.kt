package com.kikidan.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kikidan.designsystem.component.bottomnavigation.TodakunNavItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigatorTest {
    private fun navigator(start: TodakunRoute = TodakunRoute.Login) = TodakunNavigator(NavBackStack<NavKey>(start))

    @Test
    fun `초기 상태는 Login이며 인증되지 않은 상태다`() {
        val navigator = navigator()

        assertEquals(listOf(TodakunRoute.Login), navigator.backStack.toList())
        assertFalse(navigator.isAuthenticated)
    }

    @Test
    fun `파싱 실패한 딥링크는 알림함으로 폴백한다`() {
        val navigator = navigator(start = TodakunRoute.Home)

        navigator.navigateOrFallbackToDeepLink("todakun://unknown")

        assertEquals(listOf(TodakunRoute.Home, TodakunRoute.Notification), navigator.backStack.toList())
    }

    @Test
    fun `goBack은 루트가 하나 남으면 아무 동작도 하지 않는다`() {
        val navigator = navigator(start = TodakunRoute.Home)

        navigator.goBack()

        assertEquals(listOf(TodakunRoute.Home), navigator.backStack.toList())
    }

    @Test
    fun `goBack은 최상단 라우트를 제거한다`() {
        val navigator = navigator(start = TodakunRoute.Home)
        navigator.push(TodakunRoute.LuckAction)

        navigator.goBack()

        assertEquals(listOf(TodakunRoute.Home), navigator.backStack.toList())
    }

    @Test
    fun `LuckAction MyPage 탭 선택 시 Home 위에 push된다`() {
        val navigator = navigator(start = TodakunRoute.Home)

        navigator.selectTab(TodakunNavItem.LUCKY_ACTION)
        assertEquals(listOf(TodakunRoute.Home, TodakunRoute.LuckAction), navigator.backStack.toList())
        assertEquals(TodakunNavItem.LUCKY_ACTION, navigator.currentTab)

        navigator.selectTab(TodakunNavItem.MY)
        assertEquals(listOf(TodakunRoute.Home, TodakunRoute.MyPage), navigator.backStack.toList())
        assertEquals(TodakunNavItem.MY, navigator.currentTab)
    }

    @Test
    fun `Chat 탭 선택 시 Home 위에 push되고 currentTab은 null이라 바텀 내비게이션이 숨겨진다`() {
        val navigator = navigator(start = TodakunRoute.Home)

        navigator.selectTab(TodakunNavItem.TODAK_CHAT)

        assertEquals(listOf(TodakunRoute.Home, TodakunRoute.Chat()), navigator.backStack.toList())
        assertNull(navigator.currentTab)
    }

    @Test
    fun `Chat에서 goBack하면 Home으로 돌아와 바텀 내비게이션이 다시 보인다`() {
        val navigator = navigator(start = TodakunRoute.Home)
        navigator.selectTab(TodakunNavItem.TODAK_CHAT)

        navigator.goBack()

        assertEquals(listOf(TodakunRoute.Home), navigator.backStack.toList())
        assertEquals(TodakunNavItem.FORTUNE_TELLING, navigator.currentTab)
    }
}
