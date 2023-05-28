package com.jorgetargz.projectseeker.network

import com.jorgetargz.projectseeker.data.dto.users.AddDeviceDTO
import com.jorgetargz.projectseeker.data.dto.users.ChangeUserRoleDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyClientProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyFreelancerProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ProfileDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsersServices {

    @GET("secured/users/myProfile")
    suspend fun getMyProfile(): Response<ProfileDTO>

    @GET("secured/users/profile")
    suspend fun getProfile(@Query("id") userId: String): Response<ProfileDTO>

    @PUT("secured/users/changeRole")
    suspend fun changeRole(@Body changeUserRoleDTO: ChangeUserRoleDTO): Response<ProfileDTO>

    @PUT("secured/users/modifyFreelancerProfile")
    suspend fun modifyFreelancerProfile(@Body modifyFreelancerProfileDTO: ModifyFreelancerProfileDTO): Response<ProfileDTO>

    @PUT("secured/users/modifyClientProfile")
    suspend fun modifyClientProfile(@Body modifyClientProfileDTO: ModifyClientProfileDTO): Response<ProfileDTO>

    @POST("secured/users/addDevice")
    suspend fun addDevice(@Body addDeviceDTO: AddDeviceDTO): Response<Unit>

    @DELETE("secured/users/deleteMyAccount")
    suspend fun deleteAccount(): Response<Unit>
}