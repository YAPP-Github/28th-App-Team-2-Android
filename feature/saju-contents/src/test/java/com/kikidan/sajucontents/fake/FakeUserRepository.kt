package com.kikidan.sajucontents.fake

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus
import com.kikidan.domain.model.user.User
import com.kikidan.domain.model.user.WithdrawalReason
import com.kikidan.domain.repository.UserRepository
import java.time.LocalDate

/**
 * core/domain에는 test-fixtures가 구성되어 있지 않아 동일한 Fake를 모듈별로 둔다.
 */
class FakeUserRepository : UserRepository {
    var result: Result<User> =
        Result.success(
            User(
                id = "fake-id",
                name = "토닥이",
                gender = Gender.FEMALE,
                job = Job.WORKER,
                relationshipStatus = RelationshipStatus.SOLO,
                birth = Birth(DateType.SOLAR, LocalDate.of(2001, 5, 30), BirthTime.O),
            ),
        )

    override suspend fun getUserInfo(): Result<User> = result

    override suspend fun updateUserInfo(user: User): Result<Unit> = Result.success(Unit)
    override suspend fun withdraw(
        reason: WithdrawalReason,
        detail: String?
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}
