package com.jorgetargz.projectseeker.framework.users

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.jorgetargz.projectseeker.databinding.UserRowLayoutBinding
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import timber.log.Timber

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.MyViewHolder>() {

    private val client = ChatClient.instance()
    private var userList = emptyList<User>()

    class MyViewHolder(val binding: UserRowLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            UserRowLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = userList[position]

        with(holder.binding) {
            avatarImageView.setUserData(currentUser)
            usernameTextView.text = currentUser.name
            if (currentUser.online) {
                lastActiveTextView.text = "Online"
            } else {
                lastActiveTextView.text = currentUser.lastActive?.let { convertDate(it.time) } ?: "N/A"
            }
            rootLayout.setOnClickListener {
                createNewChannel(currentUser, holder)
            }
        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun setData(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    private fun convertDate(milliseconds: Long): String {
        return DateFormat.format("dd/MM/yyyy hh:mm a", milliseconds).toString()
    }

    private fun createNewChannel(selectedUser: User, holder: MyViewHolder) {
        client.createChannel(
            channelType = "messaging",
            channelId = "${selectedUser.id}-${client.getCurrentUser()!!.id}",
            memberIds = listOf(client.getCurrentUser()!!.id, selectedUser.id),
            extraData = mutableMapOf()
        ).enqueue { result ->
            if (result.isSuccess) {
                navigateToChatFragment(holder, result.data().cid)
            } else {
                Timber.e(result.error().message.toString())
            }
        }
    }

    private fun navigateToChatFragment(holder: MyViewHolder, cid: String) {
        val action = UsersFragmentDirections.actionUsersFragmentToChatFragment(cid)
        holder.itemView.findNavController().navigate(action)
    }

}











