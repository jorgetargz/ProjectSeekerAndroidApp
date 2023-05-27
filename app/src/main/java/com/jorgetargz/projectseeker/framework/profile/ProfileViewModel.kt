package com.jorgetargz.projectseeker.framework.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.data.dto.users.ChangeUserRoleDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyClientProfileDTO
import com.jorgetargz.projectseeker.data.dto.users.ModifyFreelancerProfileDTO
import com.jorgetargz.projectseeker.data.shared_preferences.EncryptedSharedPreferencesManager
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Availability
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileDataManager: ProfileDataManager,
    private val chatClient: ChatClient,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val encryptedSharedPreferencesManager: EncryptedSharedPreferencesManager
) : BaseViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile : StateFlow<Profile?> = _profile

    private val _chatClientProfile = MutableStateFlow<User?>(null)
    val chatClientProfile : StateFlow<User?> = _chatClientProfile

    private val _isEmailVerified = MutableStateFlow<Boolean?>(null)
    val isEmailVerified : StateFlow<Boolean?> = _isEmailVerified

    private val _isAccountDeleted = MutableStateFlow<Boolean?>(null)
    val isAccountDeleted : StateFlow<Boolean?> = _isAccountDeleted

    fun getMyProfile() {
        viewModelScope.launch {
            profileDataManager.getMyProfile().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _profile.value = result.data
                    }
                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun setActiveRole(activeRole: ActiveRole) {
        val changeUserRoleDTO =
            ChangeUserRoleDTO(activeRole = activeRole.toDTO())
        viewModelScope.launch {
            profileDataManager.changeRole(changeUserRoleDTO).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _profile.value = result.data
                    }
                    is NetworkResult.Error -> {
                        _isLoading.value = false
                        _errorResourceCode.value = R.string.error_change_role
                    }
                }
            }
        }
    }

    fun updateFreelancerProfile(
        availability: Availability,
        skills: List<String>,
        title: String,
        description: String
    ) {
        val modifyFreelancerProfileDTO = ModifyFreelancerProfileDTO(
                availability = availability.toDTO(),
                skills = skills,
                title = title,
                description = description
            )
        viewModelScope.launch {
            modifyFreelancerProfileDTO.let {
                profileDataManager.modifyFreelancerProfile(it).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }

                        is NetworkResult.Success -> {
                            _isLoading.value = false
                            _profile.value = result.data
                        }

                        is NetworkResult.Error -> {
                            _isLoading.value = false
                            _errorResourceCode.value = R.string.error_updating_profile
                        }
                    }
                }
            }
        }
    }

    fun updateClientProfile(title: String, description: String) {
        val modifyClientProfileDTO = ModifyClientProfileDTO(
                title = title,
                description = description
            )

        viewModelScope.launch {
            modifyClientProfileDTO.let {
                profileDataManager.modifyClientProfile(it).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }

                        is NetworkResult.Success -> {
                            _isLoading.value = false
                            _profile.value = result.data
                        }

                        is NetworkResult.Error -> {
                            _isLoading.value = false
                            _errorResourceCode.value = R.string.error_updating_profile
                        }
                    }
                }
            }
        }
    }

    fun updateProfilePic(imageUri: Uri) {
        val storageRef = firebaseStorage.reference
        val profilePictureRef =
            storageRef.child("images/${chatClient.getCurrentUser()?.id}_avatar.jpg")
        val uploadTask = profilePictureRef.putFile(imageUri)
        _isLoading.value = true
        uploadTask.addOnFailureListener {
            Timber.d("Image upload failed")
            _errorResourceCode.value = R.string.error_uploading_image
            _isLoading.value = false
        }.addOnSuccessListener {
            updateFirebaseAndStreamChatProfilePic(profilePictureRef)
        }
    }

    private fun updateFirebaseAndStreamChatProfilePic(profilePictureRef: StorageReference) {
        profilePictureRef.downloadUrl.addOnSuccessListener { downloadURL ->
            Timber.d("Image uploaded successfully")
            firebaseAuth.currentUser?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setPhotoUri(downloadURL)
                    .build()
            )?.addOnSuccessListener {
                Timber.d("Firebase profile updated successfully")
            }?.addOnFailureListener {
                Timber.d("Firebase profile not updated")
            }
            val chatClientUser = User(
                id = chatClient.getCurrentUser()?.id ?: run {
                    Timber.d("Chat client user id is null")
                    _errorResourceCode.value = R.string.error_uploading_image
                    _isLoading.value = false
                    return@addOnSuccessListener
                },
                image = downloadURL.toString()
            )
            chatClient.updateUser(chatClientUser).enqueue { result ->
                if (result.isSuccess) {
                    _chatClientProfile.value = chatClientUser
                    _isLoading.value = false
                } else {
                    Timber.d("Chat client profile not updated")
                    _errorResourceCode.value = R.string.error_uploading_image
                    _isLoading.value = false
                }
            }
        }
    }

    fun checkIfEmailIsVerified() {
        firebaseAuth.currentUser?.reload()
        _isEmailVerified.value = firebaseAuth.currentUser?.isEmailVerified
        Timber.d("Email verified: ${firebaseAuth.currentUser?.isEmailVerified}")
    }

    fun sendVerificationEmail() {
        firebaseAuth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
            _errorResourceCode.value = R.string.email_sent
        }?.addOnFailureListener {
            Timber.e(it.message, it)
            _errorResourceCode.value = R.string.email_not_sent
        }
    }

    fun isEmailVerifiedHandled() {
        _isEmailVerified.value = null
    }

    fun sendChangePasswordEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            _errorResourceCode.value = R.string.email_sent
        }.addOnFailureListener {
            Timber.e(it.message, it)
            _errorResourceCode.value = R.string.email_not_sent
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            profileDataManager.deleteAccount().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _isAccountDeleted.value = true
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                        encryptedSharedPreferencesManager.clear()
                    }
                }
            }
        }
    }

    fun isAccountDeletedHandled() {
        _isAccountDeleted.value = null
    }
}