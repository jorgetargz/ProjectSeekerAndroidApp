package com.jorgetargz.projectseeker.domain.project

enum class OfferStatus {
    PENDING, ACCEPTED, REJECTED;

    override fun toString(): String {
        return when (this) {
            PENDING -> "Pending"
            ACCEPTED -> "Accepted"
            REJECTED -> "Rejected"
        }
    }

    companion object {
        fun fromDTO(status: String): OfferStatus {
            return when (status) {
                "PENDING" -> PENDING
                "ACCEPTED" -> ACCEPTED
                else -> REJECTED
            }
        }
    }
}