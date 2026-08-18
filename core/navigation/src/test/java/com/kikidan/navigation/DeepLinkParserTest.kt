package com.kikidan.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepLinkParserTest {
    @Test
    fun `FORTUNE 딥링크는 FortuneReport로 매핑된다`() {
        val route = parseDeepLink("todakun://fortune/today")

        assertEquals(TodakunRoute.FortuneReport(fortuneId = "today"), route)
    }

    @Test
    fun `LUCKY_ACTION 딥링크는 LuckAction으로 매핑된다`() {
        val route = parseDeepLink("todakun://lucky-action")

        assertEquals(TodakunRoute.LuckAction, route)
    }

    @Test
    fun `AI_COMPLETE 딥링크는 conversationId를 포함한 Chat으로 매핑된다`() {
        val route = parseDeepLink("todakun://chat/conversations/conv-123")

        assertEquals(TodakunRoute.Chat(conversationId = "conv-123"), route)
    }

    @Test
    fun `매칭되는 패턴이 없으면 null을 반환한다`() {
        val route = parseDeepLink("todakun://notice/unknown")

        assertNull(route)
    }

    @Test
    fun `todakun 스킴이 아니면 null을 반환한다`() {
        val route = parseDeepLink("https://example.com/fortune/today")

        assertNull(route)
    }
}
