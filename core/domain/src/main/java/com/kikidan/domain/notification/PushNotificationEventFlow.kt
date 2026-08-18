package com.kikidan.domain.notification

import com.kikidan.domain.model.notification.PushNotificationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 앱이 포그라운드일 때 수신한 FCM 푸시를 인앱 이벤트로 방출하는 전역 버스.
 */
object PushNotificationEventFlow {
    private val _events = MutableSharedFlow<PushNotificationEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PushNotificationEvent> = _events.asSharedFlow()

    suspend fun emit(event: PushNotificationEvent) {
        _events.emit(event)
    }
}
