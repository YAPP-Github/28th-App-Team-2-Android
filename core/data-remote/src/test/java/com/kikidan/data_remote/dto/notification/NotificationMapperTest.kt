package com.kikidan.data_remote.dto.notification

import com.kikidan.domain.model.notification.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant

class NotificationMapperTest {
    @Test
    fun `NotificationResponse가 Notification 도메인으로 변환된다`() {
        val response =
            NotificationResponse(
                id = "n-1",
                type = "FORTUNE",
                title = "오늘의 운세가 도착했어요.",
                content = "내용",
                deepLink = "todakun://fortune",
                isRead = false,
                createdAt = "2026-07-24T00:00:00Z",
            )

        val domain = response.toDomain()

        assertEquals("n-1", domain.id)
        assertEquals(NotificationType.FORTUNE, domain.type)
        assertEquals("오늘의 운세가 도착했어요.", domain.title)
        assertEquals(Instant.parse("2026-07-24T00:00:00Z"), domain.createdAt)
        assertFalse(domain.isRead)
    }

    @Test
    fun `알 수 없는 type 문자열은 NOTICE로 근사한다`() {
        val response =
            NotificationResponse(
                id = "n-1",
                type = "UNKNOWN_TYPE",
                title = "제목",
                createdAt = "2026-07-24T00:00:00Z",
            )

        val domain = response.toDomain()

        assertEquals(NotificationType.NOTICE, domain.type)
    }

    @Test
    fun `NotificationListResponse가 NotificationSummary 도메인으로 변환된다`() {
        val response =
            NotificationListResponse(
                unreadCount = 2,
                notifications =
                    listOf(
                        NotificationResponse(
                            id = "n-1",
                            type = "LUCKY_ACTION",
                            title = "오늘 행운 액션이 열렸어요.",
                            createdAt = "2026-07-24T00:00:00Z",
                        ),
                    ),
            )

        val domain = response.toDomain()

        assertEquals(2, domain.unreadCount)
        assertEquals(1, domain.notifications.size)
        assertEquals(NotificationType.LUCKY_ACTION, domain.notifications[0].type)
    }
}
