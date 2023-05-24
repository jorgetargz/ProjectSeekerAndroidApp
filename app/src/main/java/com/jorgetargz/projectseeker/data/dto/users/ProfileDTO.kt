package com.jorgetargz.projectseeker.data.dto.users

data class ProfileDTO(
    val id: String,
    val firebaseId: String,
    val name: String,
    val email: String,
    val phone: String,
    val activeRole: String,
    val title: String,
    val description: String,
    val availability: String,
    val skills: List<String>,
)