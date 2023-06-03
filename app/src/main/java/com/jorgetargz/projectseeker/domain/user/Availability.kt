package com.jorgetargz.projectseeker.domain.user

import com.jorgetargz.projectseeker.R

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

    fun getStringResourceCode(): Int {
        return when (this) {
            FULL_TIME -> R.string.full_time
            PART_TIME -> R.string.part_time
            UNAVAILABLE -> R.string.unavailable
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

        fun fromString(availability: String): Availability {
            return when (availability) {
                "Full time" -> FULL_TIME
                "Tiempo completo" -> FULL_TIME
                "Part time" -> PART_TIME
                "Media jornada" -> PART_TIME
                else -> UNAVAILABLE
            }
        }
    }
}
