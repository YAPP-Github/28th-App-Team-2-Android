package com.kikidan.domain.model.auth

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.Gender

data class SignupSubmission(
    val name: String,
    val job: Job,
    val relationshipStatus: RelationshipStatus,
    val gender: Gender,
    val birth: Birth,
)

enum class Job {
    STUDENT,
    WORKER,
    FREELANCER,
    JOBSEEKER,
    HOMEMAKER,
    LEAVER,
}

enum class RelationshipStatus {
    SOLO,
    DATING,
    MARRY,
    REMARRY,
}
