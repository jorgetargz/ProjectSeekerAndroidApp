package com.jorgetargz.projectseeker.data.mapper

import com.jorgetargz.projectseeker.data.dto.users.ProfileDTO
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Availability
import com.jorgetargz.projectseeker.domain.user.Profile

fun ProfileDTO.toFreelancerProfile() = Profile.Freelancer(
    id = id,
    firebaseId = firebaseId,
    name = name,
    email = email,
    phone = phone,
    title = title,
    description = description,
    skills = skills,
    availability = Availability.fromDTO(availability),
    activeRole = ActiveRole.FREELANCER
)

fun ProfileDTO.toClientProfile() = Profile.Client(
    id = id,
    firebaseId = firebaseId,
    name = name,
    email = email,
    phone = phone,
    title = title,
    description = description,
    activeRole = ActiveRole.CLIENT
)