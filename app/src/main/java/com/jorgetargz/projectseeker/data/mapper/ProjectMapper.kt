package com.jorgetargz.projectseeker.data.mapper

import com.jorgetargz.projectseeker.data.dto.projects.OfferInfoDTO
import com.jorgetargz.projectseeker.data.dto.projects.ProjectInfoDTO
import com.jorgetargz.projectseeker.domain.project.Offer
import com.jorgetargz.projectseeker.domain.project.OfferStatus
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.domain.project.ProjectStatus

fun ProjectInfoDTO.toProject() = Project(
    id = id,
    clientId = clientId,
    title = title,
    description = description,
    skills = skills,
    minBudget = minBudget,
    maxBudget = maxBudget,
    startDate = startDate,
    deadlineDate = deadlineDate,
    realEndDate = realEndDate,
    status = ProjectStatus.fromDTO(status),
    selectedFreelancerId = selectedFreelancerId,
    offers = offers.map { it.toOffer() }
)

fun OfferInfoDTO.toOffer() = Offer(
    freelancerId = freelancerId,
    description = description,
    budget = budget,
    status = OfferStatus.fromDTO(status)
)