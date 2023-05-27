package com.jorgetargz.projectseeker.framework.profile

import com.jorgetargz.projectseeker.data.UsersRepository
import com.jorgetargz.projectseeker.data.dto.users.ChangeUserRoleDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyClientProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyFreelancerProfileDTO
import javax.inject.Inject

class ProfileDataManager @Inject constructor(
    private val usersRepository: UsersRepository
) {
    fun getMyProfile() = usersRepository.getMyProfile()
    fun changeRole(changeUserRoleDTO: ChangeUserRoleDTO) =
        usersRepository.changeRole(changeUserRoleDTO)

    fun modifyFreelancerProfile(modifyFreelancerProfileDTO: ModifyFreelancerProfileDTO) =
        usersRepository.modifyFreelancerProfile(modifyFreelancerProfileDTO)

    fun modifyClientProfile(modifyClientProfileDTO: ModifyClientProfileDTO) =
        usersRepository.modifyClientProfile(modifyClientProfileDTO)

    fun deleteAccount() = usersRepository.deleteAccount()
}