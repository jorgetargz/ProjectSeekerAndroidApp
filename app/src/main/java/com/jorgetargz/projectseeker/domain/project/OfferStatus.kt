package com.jorgetargz.projectseeker.domain.project

import com.jorgetargz.projectseeker.R

enum class OfferStatus {
    PENDING, ACCEPTED, REJECTED;

    override fun toString(): String {
        return when (this) {
            PENDING -> "Pending"
            ACCEPTED -> "Accepted"
            REJECTED -> "Rejected"
        }
    }

    fun getStringResourceCode(): Int {
        return when (this) {
            PENDING -> R.string.pending
            ACCEPTED -> R.string.accepted
            REJECTED -> R.string.rejected
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