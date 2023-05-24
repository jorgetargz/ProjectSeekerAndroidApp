package com.jorgetargz.projectseeker.data.dto.users

data class ModifyFreelancerProfileDTO(
    val title: String,
    val description: String,
    val skills: List<String>,
    val availability: String
)