package com.jorgetargz.projectseeker.domain.user

import com.jorgetargz.projectseeker.R

enum class ActiveRole {
    FREELANCER,
    CLIENT;

    override fun toString(): String {
        return when (this) {
            FREELANCER -> "Freelancer"
            CLIENT -> "Client"
        }
    }

    fun getStringResourceCode(): Int {
        return when (this) {
            FREELANCER -> R.string.freelancer
            CLIENT -> R.string.client
        }
    }

    fun toDTO(): String {
        return when (this) {
            FREELANCER -> "ROLE_FREELANCER"
            CLIENT -> "ROLE_CLIENT"
        }
    }

    companion object {
        fun fromString(role: String): ActiveRole {
            return when (role) {
                "Client" -> CLIENT
                "Cliente" -> CLIENT
                else -> FREELANCER
            }
        }
    }
}
