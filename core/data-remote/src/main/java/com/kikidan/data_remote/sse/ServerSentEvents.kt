package com.kikidan.data_remote.sse

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// 이벤트 간 무응답 상한. 무한으로 두면 서버가 조용히 죽었을 때 앱이 영원히 대기한다.
private const val SSE_SOCKET_TIMEOUT_MS = 60_000L

/**
 * SSE 엔드포인트를 열어 서버 이벤트를 Flow로 방출한다.
 * collect가 취소되면 연결도 닫힌다(sse()의 세션 수명이 곧 이 Flow의 수명). 비-2xx 응답은 collect 시점에 예외로 전파된다.
 */
internal fun HttpClient.serverSentEvents(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Flow<ServerSentEvent> =
    flow {
        sse(
            urlString = urlString,
            request = {
                block()
                // 전역 HttpTimeout(requestTimeoutMillis=15s)이 장시간 스트림을 끊으므로 반드시 덮어쓴다.
                // SSE 전체 길이 상한은 없애고, 소켓 무응답만 60s로 감지한다.
                timeout {
                    requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                    socketTimeoutMillis = SSE_SOCKET_TIMEOUT_MS
                }
            },
        ) {
            incoming.collect { event -> emit(event) }
        }
    }
