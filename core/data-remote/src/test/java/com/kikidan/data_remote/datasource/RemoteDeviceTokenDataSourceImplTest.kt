package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteDeviceTokenDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteDeviceTokenDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteDeviceTokenDataSourceImpl(Lazy { client })
    }

    @Test
    fun `postDeviceToken은 device-tokens 엔드포인트로 POST 요청을 보낸다`() =
        runTest {
            var requestedMethod: HttpMethod? = null
            var requestedPath: String? = null
            val body = """{"success":true,"code":"200","message":"ok","data":null}"""
            val sut =
                buildSut { request ->
                    requestedMethod = request.method
                    requestedPath = request.url.encodedPath
                    respond(body, HttpStatusCode.OK, jsonHeaders)
                }

            sut.postDeviceToken("fcm-token")

            assertEquals(HttpMethod.Post, requestedMethod)
            assertEquals("/api/v1/notifications/device-tokens", requestedPath)
        }
}
