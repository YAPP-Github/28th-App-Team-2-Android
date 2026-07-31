package com.kikidan.data_remote.util

import com.kikidan.data_remote.dto.CommonResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> HttpResponse.bodyNotNull(): T {
    val response = body<CommonResponse<T>>()
    val body = requireNotNull(response.data) {
        "${call.request.url} 응답의 data가 null입니다. code=${response.code}, message=${response.message}"
    }
    return body
}
