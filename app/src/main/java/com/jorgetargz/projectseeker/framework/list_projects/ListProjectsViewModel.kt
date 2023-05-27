package com.jorgetargz.projectseeker.framework.list_projects

import androidx.lifecycle.viewModelScope
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListProjectsViewModel @Inject constructor(
    private val listProjectsDataManager: ListProjectsDataManager
) : BaseViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _projects = MutableStateFlow<List<Project>?>(null)
    val projects: StateFlow<List<Project>?> = _projects

    fun getMyProfile() {
        viewModelScope.launch {
            listProjectsDataManager.getMyProfile().catch {
                _isLoading.value = false
                _errorResourceCode.value = R.string.error_get_my_profile
            }.collect { result ->
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

    fun loadOpenProjects() {
        viewModelScope.launch {
            listProjectsDataManager.getOpenProjects().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadMyProjects() {
        viewModelScope.launch {
            listProjectsDataManager.getMyProjects().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadMyOpenProjects() {
        viewModelScope.launch {
            listProjectsDataManager.getMyOpenProjects().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadMyProjectsInProgress() {
        viewModelScope.launch {
            listProjectsDataManager.getMyInProgressProjects().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadProjectsAssignedToMe() {
        viewModelScope.launch {
            listProjectsDataManager.getProjectsAssignedToMe().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadProjectsWhereIHaveOfferFreelancer() {
        viewModelScope.launch {
            listProjectsDataManager.getProjectsWhereIHaveOffer().catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    fun loadOpenProjectsMatchingMySkills(skills: List<String>) {
        viewModelScope.launch {
            listProjectsDataManager.getOpenProjectsBySkills(skills).catch {
                handleListProjectsNetworkException(it)
            }.collect { result ->
                handleListProjectsNetworkResult(result)
            }
        }
    }

    private fun handleListProjectsNetworkException(throwable: Throwable) {
        _isLoading.value = false
        _errorResourceCode.value = R.string.error_get_projects
        Timber.e(throwable.message, throwable)
    }

    private fun handleListProjectsNetworkResult(result: NetworkResult<List<Project>>) {
        when (result) {
            is NetworkResult.Loading -> {
                _isLoading.value = true
            }

            is NetworkResult.Success -> {
                _isLoading.value = false
                _projects.value = result.data
            }

            is NetworkResult.Error -> {
                handleNetworkError(result)
            }
        }
    }
}