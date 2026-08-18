package com.kikidan.data.datasource

import com.kikidan.domain.model.user.User

interface RemoteUserDataSource {
    suspend fun getUser(): User

    suspend fun updateUser(user: User)
}
