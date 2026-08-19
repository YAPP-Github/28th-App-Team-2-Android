package com.kikidan.data_remote.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class WithdrawMemberRequest(
    val reason: String,
    val detail: String? = null,
)
