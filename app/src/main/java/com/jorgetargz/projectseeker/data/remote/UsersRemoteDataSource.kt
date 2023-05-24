package com.jorgetargz.projectseeker.data.remote

import com.jorgetargz.projectseeker.data.dto.users.ChangeUserRoleDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyClientProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyFreelancerProfileDTO
import com.jorgetargz.projectseeker.network.UsersServices
import javax.inject.Inject

class UsersRemoteDataSource @Inject constructor(
    private val usersServices: UsersServices,
    private val safeSpringApiCall: SafeSpringApiCall
)  {

    suspend fun getMyProfile() = safeSpringApiCall.safeApiCall { usersServices.getMyProfile() }

    suspend fun getProfile(userId: String) = safeSpringApiCall.safeApiCall { usersServices.getProfile(userId) }

    suspend fun changeRole(changeUserRoleDTO: ChangeUserRoleDTO) =
        safeSpringApiCall.safeApiCall { usersServices.changeRole(changeUserRoleDTO) }

    suspend fun modifyFreelancerProfile(modifyFreelancerProfileDTO: ModifyFreelancerProfileDTO) =
        safeSpringApiCall.safeApiCall { usersServices.modifyFreelancerProfile(modifyFreelancerProfileDTO) }

    suspend fun modifyClientProfile(modifyClientProfileDTO: ModifyClientProfileDTO) =
        safeSpringApiCall.safeApiCall { usersServices.modifyClientProfile(modifyClientProfileDTO) }
}