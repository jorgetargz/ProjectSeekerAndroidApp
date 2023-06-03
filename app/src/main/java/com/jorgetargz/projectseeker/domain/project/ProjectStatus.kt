package com.jorgetargz.projectseeker.domain.project

import com.jorgetargz.projectseeker.R

enum class ProjectStatus {
    OPEN, FINISHED, IN_PROGRESS;

    override fun toString(): String {
        return when (this) {
            OPEN -> "Open"
            FINISHED -> "Finished"
            IN_PROGRESS -> "In progress"
        }
    }

    fun getStringResourceCode(): Int {
        return when (this) {
            OPEN -> R.string.open
            FINISHED -> R.string.finished
            IN_PROGRESS -> R.string.in_progress
        }
    }

    companion object {
        fun fromDTO(status: String): ProjectStatus {
            return when (status) {
                "OPEN" -> OPEN
                "FINISHED" -> FINISHED
                else -> IN_PROGRESS
            }
        }
    }
}