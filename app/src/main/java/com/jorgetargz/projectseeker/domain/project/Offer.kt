package com.jorgetargz.projectseeker.domain.project

data class Offer(
    val freelancerId: String,
    val description: String,
    val budget: Double,
    val status: OfferStatus
)
