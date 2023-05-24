package com.jorgetargz.projectseeker.domain.project

import java.time.LocalDate

data class Project(
    val id: String,
    val clientId: String,
    val title: String,
    val description: String,
    val skills: List<String>,
    val minBudget: Double,
    val maxBudget: Double,
    val startDate: LocalDate,
    val deadlineDate: LocalDate,
    val realEndDate: LocalDate?,
    val status: ProjectStatus,
    val selectedFreelancerId: String?,
    val offers : List<Offer>
)