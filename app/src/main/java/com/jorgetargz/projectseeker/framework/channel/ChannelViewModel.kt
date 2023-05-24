package com.jorgetargz.projectseeker.framework.channel

import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val chatClient: ChatClient
) : BaseViewModel() {

    fun deleteChannel(cid: String) {
        val channelClient = chatClient.channel("messaging", cid)
        channelClient.delete().enqueue { result ->
            if (result.isSuccess) {
                _errorResourceCode.value = R.string.channel_deleted
            } else {
                _errorResourceCode.value = R.string.channel_delete_error
            }
        }
    }

}