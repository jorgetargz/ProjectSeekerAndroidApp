package com.jorgetargz.projectseeker.data.dto.projects

import java.time.LocalDate

data class ProjectInfoDTO(
    val id: String,
    val clientId: String,
    val title: String,
    val description: String,
    val skills: List<String>,
    val minBudget: Double,
    val maxBudget: Double,
    val startDate: LocalDate,
    val deadlineDate: LocalDate,
    val realEndDate: LocalDate,
    val status: String,
    val selectedFreelancerId: String,
    val offers: List<OfferInfoDTO>
)
