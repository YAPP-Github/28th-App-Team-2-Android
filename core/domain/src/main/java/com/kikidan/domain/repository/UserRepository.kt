package com.kikidan.domain.repository

import com.kikidan.domain.model.user.User

interface UserRepository {
    suspend fun getUserInfo(): Result<User>
}
