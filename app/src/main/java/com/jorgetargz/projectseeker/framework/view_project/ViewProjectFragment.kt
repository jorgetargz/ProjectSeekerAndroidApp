package com.jorgetargz.projectseeker.framework.view_project

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentViewProjectBinding
import com.jorgetargz.projectseeker.domain.project.Offer
import com.jorgetargz.projectseeker.domain.project.OfferStatus
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.domain.project.ProjectStatus
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import com.jorgetargz.projectseeker.framework.common.adapters.offers.OffersActions
import com.jorgetargz.projectseeker.framework.common.adapters.offers.OffersAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ViewProjectFragment : BaseFragment() {

    private val viewModel: ViewProjectViewModel by viewModels()
    private val args: ViewProjectFragmentArgs by navArgs()
    private lateinit var binding: FragmentViewProjectBinding

    inner class OffersActionsImpl : OffersActions {
        override fun onAcceptOffer(freelancerId: String) {
            viewModel.acceptOffer(args.projectId, freelancerId)
        }

        override fun onViewProfile(freelancerId: String) {
            viewModel.viewFreelancerProfile(freelancerId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupBiniding()
        viewModel.getMyProfile()
        viewModel.getProjectInfo(args.projectId)
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewProjectState.collect { viewProjectState ->
                    if (viewProjectState.project != null && viewProjectState.viewerProfile != null) {
                        setupContent(viewProjectState.project, viewProjectState.viewerProfile)
                    }
                    if (viewProjectState.clientProfile != null) {
                        binding.tvProjectClient.text = viewProjectState.clientProfile.name
                        setupChatWithClientButton(viewProjectState.clientProfile.firebaseId)
                    }
                    if (viewProjectState.assignedFreelancer != null) {
                        binding.tvProjectFreelancer.text = viewProjectState.assignedFreelancer.name
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatCID.collect { chatCID ->
                    chatCID?.let {
                        val action = ViewProjectFragmentDirections.actionViewProjectFragmentToChatFragment(chatCID)
                        findNavController().navigate(action)
                        viewModel.resetChatCID()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewFreelancerProfileState.collect { profile ->
                    profile?.let {
                        if (profile is Profile.Freelancer){
                            showFreelancerProfileDialog(profile)
                        } else {
                            showSnackbar(getString(R.string.error_viewing_profile))
                        }
                        viewModel.resetViewFreelancerProfileState()
                    }
                }
            }
        }
        observeLoading(viewModel)
        observeErrorResourceCode(viewModel)
        observeErrorString(viewModel)
    }

    private fun showFreelancerProfileDialog(profile: Profile.Freelancer) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_profile, binding.root, false)
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileName).text = profile.name
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileTitle).text = profile.title
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileDescription).text = profile.description
        dialogView.findViewById<MaterialTextView>(R.id.tvSkills).text = profile.skills.joinToString(", ")

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.open_chat)) { dialog, _ ->
                viewModel.createOrObtainCIDOfChannelByUserId(profile.firebaseId)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun setupChatWithClientButton(clientFirebaseId: String) {
        with(binding) {
            btnChatWithClient.visibility = View.VISIBLE
            btnChatWithClient.setOnClickListener {
                viewModel.createOrObtainCIDOfChannelByUserId(clientFirebaseId)
            }
        }
    }

    private fun setupContent(project: Project, profile: Profile) {
        with(binding) {
            btnFinishProject.visibility = View.GONE
            btnChatWithClient.visibility = View.GONE
            btnSubmitOffer.visibility = View.GONE
            rvOffers.visibility = View.GONE
            tvOffersLabel.visibility = View.GONE

            tvProjectTitle.text = project.title
            tvProjectDescription.text = project.description
            tvProjectCreatedDate.text = project.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            tvProjectDeadline.text = project.deadlineDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            tvProjectSkills.text = project.skills.joinToString(", ")
            val budget = if (project.minBudget == project.maxBudget) {
                project.minBudget.toString()
            } else {
                "${project.minBudget}€ - ${project.maxBudget}€"
            }
            tvProjectBudget.text = budget
            tvProjectStatus.text = project.status.toString()

            if (project.clientId == profile.id) {
                tvProjectClient.text = profile.name
                if (project.status == ProjectStatus.IN_PROGRESS) {
                    btnFinishProject.visibility = View.VISIBLE
                    btnFinishProject.setOnClickListener {
                        viewModel.finishProject(project.id)
                    }
                }
                btnChatWithClient.visibility = View.GONE
                rvOffers.visibility = View.VISIBLE
                tvOffersLabel.visibility = View.VISIBLE
                setupAdapter(project.offers)
            } else {
                viewModel.getClientProfileInfo(project.clientId)
            }

            if (profile.activeRole == ActiveRole.FREELANCER) {
                btnChatWithClient.visibility = View.VISIBLE
                btnSubmitOffer.visibility = View.VISIBLE
                btnSubmitOffer.setOnClickListener {
                    showSubmitOfferDialog(project.id, profile.id)
                }
            }

            when (project.selectedFreelancerId) {
                null -> {
                    tvProjectFreelancer.text = getString(R.string.no_freelancer_selected)
                }
                profile.id -> {
                    tvProjectFreelancer.text = profile.name
                }
                else -> {
                    viewModel.getAssignedFreelancerProfileInfo(project.selectedFreelancerId)
                }
            }
        }
    }

    private fun showSubmitOfferDialog(projectId: String, profileId: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_submit_offer)
            .setPositiveButton(getString(R.string.submit)) { dialog1, _ ->
                val dialog = dialog1 as Dialog
                val offer = Offer(
                    freelancerId = profileId,
                    budget = dialog.findViewById<EditText>(R.id.etBudget).text.toString().toDouble(),
                    description = dialog.findViewById<EditText>(R.id.etDescription).text.toString(),
                    status = OfferStatus.PENDING
                )
                viewModel.submitOffer(offer, projectId)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()
        dialog.show()
    }

    private fun setupAdapter(offers: List<Offer>) {
        val adapter = OffersAdapter(OffersActionsImpl())
        binding.rvOffers.adapter = adapter
        binding.rvOffers.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter.submitList(offers)
    }

    private fun setupBiniding() {
        binding = FragmentViewProjectBinding.inflate(layoutInflater)
    }

}