package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
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
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteYearFortuneDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteYearFortuneDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteYearFortuneDataSourceImpl(Lazy { client })
    }

    @Test
    fun `postYearFortune을_호출하면_정상_응답이_YearFortune_도메인_모델로_반환된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok",""" +
                    """"data":{"id":"f-1","year":2026,"score":85,"title":"t","content":"c",""" +
                    """"fortuneCategories":[{"fortuneCategory":"MONEY","star":3}]}}"""
            val sut =
                buildSut { request ->
                    // 메서드/경로가 REST 엔드포인트(POST /api/v1/year-fortunes/{year})와 어긋나도
                    // 통과하지 않도록 요청 자체를 검증한다.
                    assertEquals(HttpMethod.Post, request.method)
                    assertEquals("/api/v1/year-fortunes/2026", request.url.encodedPath)
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            val result = sut.postYearFortune(2026)

            // then
            assertEquals(
                YearFortune(
                    id = "f-1",
                    year = 2026,
                    score = 85,
                    title = "t",
                    content = "c",
                    categories = listOf(FortuneCategoryStar(FortuneCategory.MONEY, 3)),
                ),
                result,
            )
        }

    @Test
    fun `연도별_운세_응답이_401이면_예외가_삼켜지지_않고_그대로_throw된다`() =
        runTest {
            // given
            val sut =
                buildSut {
                    respond("", HttpStatusCode.Unauthorized)
                }

            // when
            val result = runCatching { sut.postYearFortune(2026) }

            // then
            assertTrue(result.isFailure)
        }

    @Test
    fun `getYearFortune을_호출하면_id_기준_GET_요청으로_YearFortune_도메인_모델이_반환된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok",""" +
                    """"data":{"id":"f-1","year":2026,"score":85,"title":"t","content":"c",""" +
                    """"fortuneCategories":[{"fortuneCategory":"MONEY","star":3}]}}"""
            val sut =
                buildSut { request ->
                    // GET /api/v1/year-fortunes/{id}와 어긋나도 통과하지 않도록 요청 자체를 검증한다.
                    assertEquals(HttpMethod.Get, request.method)
                    assertEquals("/api/v1/year-fortunes/f-1", request.url.encodedPath)
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            val result = sut.getYearFortune("f-1")

            // then
            assertEquals(
                YearFortune(
                    id = "f-1",
                    year = 2026,
                    score = 85,
                    title = "t",
                    content = "c",
                    categories = listOf(FortuneCategoryStar(FortuneCategory.MONEY, 3)),
                ),
                result,
            )
        }

    @Test
    fun `getYearFortune_응답이_404이면_예외가_삼켜지지_않고_그대로_throw된다`() =
        runTest {
            // given
            val sut =
                buildSut {
                    respond("", HttpStatusCode.NotFound)
                }

            // when
            val result = runCatching { sut.getYearFortune("missing-id") }

            // then
            assertTrue(result.isFailure)
        }
}
