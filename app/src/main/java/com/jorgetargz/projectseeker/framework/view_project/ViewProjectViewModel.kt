package com.jorgetargz.projectseeker.framework.view_project

import androidx.lifecycle.viewModelScope
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.data.dto.projects.AssignProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.SubmitOfferDTO
import com.jorgetargz.projectseeker.domain.project.Offer
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ViewProjectViewModel @Inject constructor(
    private val viewProjectDataManager: ViewProjectDataManager,
    private val chatClient: ChatClient
) : BaseViewModel() {

    private val _viewProjectState = MutableStateFlow(ViewProjectState())
    val viewProjectState: StateFlow<ViewProjectState> = _viewProjectState

    private val _chatCID = MutableStateFlow<String?>(null)
    val chatCID: StateFlow<String?> = _chatCID

    private val _viewProfileState = MutableStateFlow<Profile?>(null)
    val viewProfileState: StateFlow<Profile?> = _viewProfileState

    private val _offerSubmitted = MutableStateFlow<Offer?>(null)
    val offerSubmitted : StateFlow<Offer?> = _offerSubmitted

    fun getProjectInfo(projectId: String) {
        viewModelScope.launch {
            viewProjectDataManager.getProjectInfo(projectId).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_get_projects
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            project = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun getMyProfile() {
        viewModelScope.launch {
            viewProjectDataManager.getMyProfile().catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_get_my_profile
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            viewerProfile = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun getClientProfileInfo(clientId: String) {
        viewModelScope.launch {
            viewProjectDataManager.getProfile(clientId).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_get_projects
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            clientProfile = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun getAssignedFreelancerProfileInfo(selectedFreelancerId: String) {
        viewModelScope.launch {
            viewProjectDataManager.getProfile(selectedFreelancerId).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_get_profile
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            assignedFreelancer = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    private fun createOrObtainCIDOfChannelByMembers(freelancerFirebaseId: String, clientFirebaseId: String) {
        //Try to find the messaging channel
        val request = QueryChannelsRequest(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.eq("members", listOf(freelancerFirebaseId, clientFirebaseId)),
            ),
            offset = 0,
            limit = 1
        ).apply {
            watch = true
            state = true
        }

        chatClient.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                val channels: List<Channel> = result.data()
                if (channels.isNotEmpty()) {
                    val cid = channels[0].cid
                    _chatCID.value = cid
                } else {
                    //If the channel doesn't exist, create it
                    chatClient.createChannel(
                        channelType = "messaging",
                        channelId = "${freelancerFirebaseId}-${clientFirebaseId}",
                        memberIds = listOf(freelancerFirebaseId, clientFirebaseId),
                        extraData = mutableMapOf()
                    ).enqueue { resultCreateChannel ->
                        if (resultCreateChannel.isSuccess) {
                            val cid = resultCreateChannel.data().cid
                            _chatCID.value = cid
                        } else {
                            Timber.e(resultCreateChannel.error().message.toString())
                        }
                    }
                }
            } else {
                Timber.e(result.error().message.toString())
                _errorResourceCode.value = R.string.error_opening_chat
            }
        }
    }

    fun resetChatCID() {
        _chatCID.value = null
    }

    fun acceptOffer(projectId: String, freelancerId: String) {
        viewModelScope.launch {
            val assignProjectDTO = AssignProjectDTO(
                projectId = projectId,
                freelancerId = freelancerId
            )
            viewProjectDataManager.assignFreelancer(assignProjectDTO).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_accept_offer
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            project = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun viewProfile(userId: String) {
        viewModelScope.launch {
            viewProjectDataManager.getProfile(userId).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_get_profile
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProfileState.value = result.data
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun submitOffer(offer: Offer, projectId: String) {
        if (offer.budget == 0.0 || offer.description.isBlank()) {
            _errorResourceCode.value = R.string.error_empty_fields
            return
        }
        viewModelScope.launch {
            val submitOfferDTO = SubmitOfferDTO(
                budget = offer.budget,
                description = offer.description,
                projectId = projectId
            )
            viewProjectDataManager.submitOffer(submitOfferDTO).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_sending_offer
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            project = result.data
                        )
                        _offerSubmitted.value = offer
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun finishProject(id: String) {
        viewModelScope.launch {
            viewProjectDataManager.finishProject(id).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_finish_project
            }.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                        _viewProjectState.value = _viewProjectState.value.copy(
                            project = result.data
                        )
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(result)
                    }
                }
            }
        }
    }

    fun createOrObtainCIDOfChannelByUserId(userId: String) {
        createOrObtainCIDOfChannelByMembers(userId, chatClient.getCurrentUser()!!.id)
    }

    fun resetViewProfileState() {
        _viewProfileState.value = null
    }

    fun resetOfferSubmitted() {
        _offerSubmitted.value = null
    }
}