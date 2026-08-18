package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteUserDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.common.toApiValue
import com.kikidan.data_remote.dto.user.MyProfileResponse
import com.kikidan.data_remote.dto.user.UpdateMemberRequest
import com.kikidan.data_remote.dto.user.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.user.User
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import javax.inject.Inject

class RemoteUserDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteUserDataSource {
        override suspend fun getUser(): User =
            client.get().get(MEMBERS_ME_URL).bodyNotNull<MyProfileResponse>().toDomain()

        override suspend fun updateUser(user: User) {
            val request =
                UpdateMemberRequest(
                    gender = user.gender.name,
                    calendarType = user.birth.dateType.name,
                    birthDate = user.birth.date.toString(),
                    birthTime = user.birth.time.toApiValue(),
                    job = user.job.name,
                    relationshipStatus = user.relationshipStatus.name,
                )
            client.get().patch(MEMBERS_ME_URL) { setBody(request) }.body<CommonResponse<Unit>>()
        }

        companion object {
            private const val MEMBERS_ME_URL = "api/v1/members/me"
        }
    }
