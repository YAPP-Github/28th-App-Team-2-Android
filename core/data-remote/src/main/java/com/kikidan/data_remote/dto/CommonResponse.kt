package com.kikidan.data_remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommonResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
    val timestamp: String? = null,
)
