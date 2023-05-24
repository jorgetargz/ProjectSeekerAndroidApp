package com.jorgetargz.projectseeker.data.dto.projects

import java.time.LocalDate

data class CreateProjectDTO(
    val title: String,
    val description: String,
    val skills: List<String>,
    val minBudget: Double,
    val maxBudget: Double,
    val startDate: LocalDate,
    val deadlineDate: LocalDate
)
