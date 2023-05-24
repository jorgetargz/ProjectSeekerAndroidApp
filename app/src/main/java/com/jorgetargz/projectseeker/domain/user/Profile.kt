package com.jorgetargz.projectseeker.domain.user

sealed class Profile {
    abstract val id: String
    abstract val firebaseId: String
    abstract val activeRole: ActiveRole
    abstract val name: String
    abstract val email: String
    abstract val phone: String
    abstract val title: String
    abstract val description: String

    data class Freelancer(
        override val id: String,
        override val firebaseId: String,
        override val activeRole: ActiveRole,
        override val name: String,
        override val email: String,
        override val phone: String,
        override val title: String,
        override val description: String,
        val availability: Availability,
        val skills: List<String>,
    ) : Profile()

    data class Client(
        override val id: String,
        override val firebaseId: String,
        override val activeRole: ActiveRole,
        override val name: String,
        override val email: String,
        override val phone: String,
        override val title: String,
        override val description: String,
    ) : Profile()
}
