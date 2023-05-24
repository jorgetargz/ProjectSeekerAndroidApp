package com.jorgetargz.projectseeker.framework.channel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentChannelBinding
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory

@AndroidEntryPoint
class ChannelFragment : BaseFragment() {

    private val viewModel : ChannelViewModel by viewModels()
    private var _binding: FragmentChannelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChannelBinding.inflate(inflater, container, false)

        setupChannels()
        observeViewModel()

        with(binding){
            with(channelsView) {
                setChannelItemClickListener { channel ->
                    val action = ChannelFragmentDirections.actionChannelFragmentToChatFragment(channel.cid)
                    findNavController().navigate(action)
                }
                setChannelDeleteClickListener { channel ->
                    viewModel.deleteChannel(channel.id)
                }
            }
            with(channelListHeaderView) {
                setOnActionButtonClickListener {
                    findNavController().navigate(R.id.action_channelFragment_to_usersFragment)
                }
                setOnlineTitle(getString(R.string.online))
            }
            return root
        }
    }

    private fun observeViewModel() {
        observeLoading(viewModel)
        observeErrorString(viewModel)
        observeErrorResourceCode(viewModel)
    }

    private fun setupChannels() {
        val viewModelFactory = ChannelListViewModelFactory(
            sort = ChannelListViewModel.DEFAULT_SORT
        )
        val listViewModel: ChannelListViewModel by viewModels { viewModelFactory }
        val listHeaderViewModel: ChannelListHeaderViewModel by viewModels()

        listHeaderViewModel.bindView(binding.channelListHeaderView, viewLifecycleOwner)
        listViewModel.bindView(binding.channelsView, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}