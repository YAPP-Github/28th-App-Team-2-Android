package com.kikidan.data_remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemberRequest(
    val gender: String,
    val calendarType: String,
    val birthDate: String,
    val birthTime: String,
    val job: String,
    val relationshipStatus: String,
)
