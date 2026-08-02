package com.kikidan.data_remote.datasource

import com.kikidan.data_remote.di.TodakunJson
import com.kikidan.data_remote.di.installTodakunDefaults
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RemoteAuthDataSourceImplTest {
    private val json = TodakunJson
    private val baseUrl = "https://test.example.com/"
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildSut(handler: MockRequestHandler): RemoteAuthDataSourceImpl {
        val engine = MockEngine(handler)
        val client = HttpClient(engine) { installTodakunDefaults(json, baseUrl) }
        return RemoteAuthDataSourceImpl(Lazy { client })
    }

    @Test
    fun `postRefresh를_호출하면_정상_응답이_AuthToken_도메인_모델로_반환된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok",""" +
                    """"data":{"accessToken":"a-1","refreshToken":"r-1"}}"""
            val sut =
                buildSut {
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            val result = sut.postRefresh("old-refresh")

            // then
            assertEquals(AuthToken("a-1", "r-1"), result)
        }

    @Test
    fun `postSignup을_호출하면_정상_응답이_AuthToken_도메인_모델로_반환된다`() =
        runTest {
            // given
            val body =
                """{"success":true,"code":"200","message":"ok",""" +
                    """"data":{"accessToken":"a-1","refreshToken":"r-1"}}"""
            val sut =
                buildSut {
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }
            val signupSubmission =
                SignupSubmission(
                    name = "홍길동",
                    job = Job.STUDENT,
                    relationshipStatus = RelationshipStatus.SOLO,
                    gender = Gender.MALE,
                    birth = Birth(dateType = DateType.SOLAR, date = LocalDate.of(2000, 1, 1), time = BirthTime.JA),
                )

            // when
            val result = sut.postSignup(signupSubmission, OnboardingToken("token-1"))

            // then
            assertEquals(AuthToken("a-1", "r-1"), result)
        }

    @Test
    fun `서버_응답에_알_수_없는_필드가_포함돼도_ignoreUnknownKeys로_파싱에_성공한다`() =
        runTest {
            // given
            val sut =
                buildSut {
                    val body =
                        """{"success":true,"code":"200","message":"ok",""" +
                            """"data":{"accessToken":"a-1","refreshToken":"r-1","unknownField":"v"},""" +
                            """"extraTopLevel":123}"""
                    respond(
                        content = body,
                        status = HttpStatusCode.OK,
                        headers = jsonHeaders,
                    )
                }

            // when
            val result = runCatching { sut.postRefresh("old-refresh") }

            // then
            assertTrue(result.isSuccess)
        }

    @Test
    fun `refresh_응답이_401이면_예외가_삼켜지지_않고_그대로_throw된다`() =
        runTest {
            // given
            val sut =
                buildSut {
                    respond("", HttpStatusCode.Unauthorized)
                }

            // when
            val result = runCatching { sut.postRefresh("old-refresh") }

            // then
            assertTrue(result.isFailure)
        }
}
