package com.jorgetargz.projectseeker.domain.user

enum class Availability {
    FULL_TIME,
    PART_TIME,
    UNAVAILABLE;

    override fun toString(): String {
        return when (this) {
            FULL_TIME -> "Full time"
            PART_TIME -> "Part time"
            UNAVAILABLE -> "Unavailable"
        }
    }

    fun toDTO(): String {
        return when (this) {
            FULL_TIME -> "FULL_TIME"
            PART_TIME -> "PART_TIME"
            UNAVAILABLE -> "UNAVAILABLE"
        }
    }

    companion object {
        fun fromDTO(availability: String): Availability {
            return when (availability) {
                "FULL_TIME" -> FULL_TIME
                "PART_TIME" -> PART_TIME
                else -> UNAVAILABLE
            }
        }
    }
}
