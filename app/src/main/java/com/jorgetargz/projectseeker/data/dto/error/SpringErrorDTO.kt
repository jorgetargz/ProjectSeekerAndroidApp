package com.jorgetargz.projectseeker.data.dto.error

import java.sql.Timestamp

data class SpringErrorDTO(
    val timestamp: Timestamp,
    val httpErrorCode: Int,
    val firebaseError: String,
    val message: String,
    val description: String
)