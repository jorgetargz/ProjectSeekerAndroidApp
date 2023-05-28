package com.jorgetargz.projectseeker.data.dto.users

import android.os.Build

data class AddDeviceDTO(
    val deviceToken: String,
    val deviceModel: String = Build.MODEL
)
