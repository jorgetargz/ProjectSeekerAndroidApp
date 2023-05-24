package com.jorgetargz.projectseeker.domain.project

enum class ProjectStatus {
    OPEN, FINISHED, IN_PROGRESS;

    override fun toString(): String {
        return when (this) {
            OPEN -> "Open"
            FINISHED -> "Finished"
            IN_PROGRESS -> "In progress"
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