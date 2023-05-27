package com.jorgetargz.projectseeker.data

import com.jorgetargz.projectseeker.data.dto.users.ChangeUserRoleDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyClientProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyFreelancerProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ProfileDTO
import com.jorgetargz.projectseeker.data.mapper.toClientProfile
import com.jorgetargz.projectseeker.data.mapper.toFreelancerProfile
import com.jorgetargz.projectseeker.data.remote.UsersRemoteDataSource
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val usersRemoteDataSource: UsersRemoteDataSource
) {

    fun getMyProfile(): Flow<NetworkResult<Profile>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.getMyProfile()
            emit(profileNetworkResult(result))
        }.flowOn(Dispatchers.IO)
    }

    fun getProfile(userId: String): Flow<NetworkResult<Profile>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.getProfile(userId)
            emit(profileNetworkResult(result))
        }.flowOn(Dispatchers.IO)
    }

    fun changeRole(changeUserRoleDTO: ChangeUserRoleDTO) : Flow<NetworkResult<Profile>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.changeRole(changeUserRoleDTO)
            emit(profileNetworkResult(result))
        }.flowOn(Dispatchers.IO)
    }

    fun modifyFreelancerProfile(modifyFreelancerProfileDTO: ModifyFreelancerProfileDTO) : Flow<NetworkResult<Profile>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.modifyFreelancerProfile(modifyFreelancerProfileDTO)
            emit(profileNetworkResult(result))
        }.flowOn(Dispatchers.IO)
    }

    fun modifyClientProfile(modifyClientProfileDTO: ModifyClientProfileDTO) : Flow<NetworkResult<Profile>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.modifyClientProfile(modifyClientProfileDTO)
            emit(profileNetworkResult(result))
        }.flowOn(Dispatchers.IO)
    }

    fun deleteAccount() : Flow<NetworkResult<Unit>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = usersRemoteDataSource.deleteAccount()
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    private fun profileNetworkResult(result: NetworkResult<ProfileDTO>) =
        result.map { result1 ->
            when (result1.activeRole) {
                ActiveRole.FREELANCER.toDTO() -> {
                    result1.toFreelancerProfile()
                }

                ActiveRole.CLIENT.toDTO() -> {
                    result1.toClientProfile()
                }

                else -> {
                    throw RuntimeException("Invalid role")
                }
            }
        }
}