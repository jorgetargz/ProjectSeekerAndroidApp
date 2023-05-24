package com.jorgetargz.projectseeker.domain.user

enum class ActiveRole {
    FREELANCER,
    CLIENT;

    override fun toString(): String {
        return when (this) {
            FREELANCER -> "Freelancer"
            CLIENT -> "Client"
        }
    }

    fun toDTO(): String {
        return when (this) {
            FREELANCER -> "ROLE_FREELANCER"
            CLIENT -> "ROLE_CLIENT"
        }
    }
}
