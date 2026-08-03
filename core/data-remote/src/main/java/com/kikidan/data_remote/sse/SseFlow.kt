package com.kikidan.data_remote.sse

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.sse.ServerSentEvent
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// 이벤트 간 무응답 상한. 무한으로 두면 서버가 조용히 죽었을 때 앱이 영원히 대기한다.
private const val SSE_SOCKET_TIMEOUT_MS = 60_000L

/**
 * SSE 엔드포인트를 열어 서버 이벤트를 Flow로 방출한다.
 * collect가 취소되면 연결도 닫힌다. 비-2xx 응답은 collect 시점에 예외로 전파된다.
 *
 * ponytail: serverSentEventsSession 대신 bodyAsChannel 수동 파싱 사용.
 * MockEngine이 SSECapability를 선언하지 않아 테스트가 불가해 이 경로를 채택함.
 * OkHttp 실기기에서 스트리밍이 정상이면 이대로 유지.
 */
internal fun HttpClient.serverSentEvents(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): Flow<ServerSentEvent> =
    flow {
        prepareRequest {
            url(urlString)
            block()
            // 전역 HttpTimeout(requestTimeoutMillis=15s)이 장시간 스트림을 끊으므로 반드시 덮어쓴다.
            // SSE 전체 길이 상한은 없애고, 소켓 무응답만 60s로 감지한다.
            timeout {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = SSE_SOCKET_TIMEOUT_MS
            }
        }.execute { response ->
            val channel = response.bodyAsChannel()
            var eventName: String? = null
            val dataLines = StringBuilder()
            var id: String? = null
            var retry: Long? = null

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break
                when {
                    line.startsWith("event:") -> eventName = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> {
                        if (dataLines.isNotEmpty()) dataLines.append('\n')
                        dataLines.append(line.removePrefix("data:").trim())
                    }
                    line.startsWith("id:") -> id = line.removePrefix("id:").trim()
                    line.startsWith("retry:") -> retry = line.removePrefix("retry:").trim().toLongOrNull()
                    line.isEmpty() && (dataLines.isNotEmpty() || eventName != null) -> {
                        emit(
                            ServerSentEvent(
                                data = dataLines.toString().takeIf { it.isNotEmpty() },
                                event = eventName,
                                id = id,
                                retry = retry,
                            )
                        )
                        eventName = null
                        dataLines.clear()
                        id = null
                        retry = null
                    }
                }
            }
        }
    }
