package com.jorgetargz.projectseeker.framework.view_project

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
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
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ViewProjectFragment : BaseFragment() {

    private val viewModel: ViewProjectViewModel by viewModels()
    private val args: ViewProjectFragmentArgs by navArgs()
    private val binding: FragmentViewProjectBinding by lazy {
        FragmentViewProjectBinding.inflate(layoutInflater)
    }

    inner class OffersActionsImpl : OffersActions {
        override fun onAcceptOffer(freelancerId: String) {
            viewModel.acceptOffer(args.projectId, freelancerId)
        }

        override fun onViewProfile(freelancerId: String) {
            viewModel.viewProfile(freelancerId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.getMyProfile()
        viewModel.getProjectInfo(args.projectId)
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        observeStateFlowOnStarted {
            viewModel.viewProjectState.collect { viewProjectState ->
                if (viewProjectState.project != null && viewProjectState.viewerProfile != null) {
                    setupContent(viewProjectState.project, viewProjectState.viewerProfile)
                }
                if (viewProjectState.clientProfile != null) {
                    binding.tvProjectClient.text = viewProjectState.clientProfile.name
                    setupChatWithClientButton(viewProjectState.clientProfile.firebaseId)
                    setupViewClientProfileButton(viewProjectState.clientProfile.id)
                }
                if (viewProjectState.assignedFreelancer != null) {
                    binding.tvProjectFreelancer.text = viewProjectState.assignedFreelancer.name
                }
            }
        }
        observeStateFlowOnStarted {
            viewModel.chatCID.collect { chatCID ->
                chatCID?.let {
                    val action =
                        ViewProjectFragmentDirections.actionViewProjectFragmentToChatFragment(
                            chatCID
                        )
                    findNavController().navigate(action)
                    binding.btnChatWithClient.isEnabled = true
                    viewModel.resetChatCID()
                }
            }
        }
        observeStateFlowOnStarted {
            viewModel.viewProfileState.collect { profile ->
                profile?.let {
                    if (profile is Profile.Freelancer) {
                        showFreelancerProfileDialog(profile)
                    } else if (profile is Profile.Client) {
                        showCLientProfileDialog(profile)
                    } else {
                        showSnackbar(getString(R.string.error_viewing_profile))
                    }
                    viewModel.resetViewProfileState()
                }
            }
        }
        observeStateFlowOnStarted {
            viewModel.offerSubmitted.collect { offer ->
                offer?.let {
                    showOfferDialog(offer)
                    viewModel.resetOfferSubmitted()
                }
            }
        }
        observeLoading(viewModel.isLoading)
        observeErrorString(viewModel.errorString) { viewModel.errorStringHandled() }
        observeErrorResourceCode(viewModel.errorResourceCode) { viewModel.errorResourceCodeHandled() }
    }

    private fun showOfferDialog(offer: Offer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.offer_submitted))
            .setMessage(offer.description + " " + getString(R.string.project_budget_label) + " " + offer.budget)
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun setupViewClientProfileButton(clientId: String) {
        binding.btnViewClientProfile.setOnClickListener {
            viewModel.viewProfile(clientId)
        }
    }

    private fun showFreelancerProfileDialog(profile: Profile.Freelancer) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_profile, binding.root, false)
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileName).text = profile.name
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileTitle).text = profile.title
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileDescription).text =
            profile.description
        dialogView.findViewById<MaterialTextView>(R.id.tvSkills).text =
            profile.skills.joinToString(", ")

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

    private fun showCLientProfileDialog(profile: Profile.Client) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_profile, binding.root, false)
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileName).text = profile.name
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileTitle).text = profile.title
        dialogView.findViewById<MaterialTextView>(R.id.tvProfileDescription).text =
            profile.description
        dialogView.findViewById<MaterialTextView>(R.id.tvSkills).visibility = View.GONE
        dialogView.findViewById<MaterialTextView>(R.id.tvSkillsLabel).visibility = View.GONE

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
                btnChatWithClient.isEnabled = false
                viewModel.createOrObtainCIDOfChannelByUserId(clientFirebaseId)
            }
        }
    }

    private fun setupContent(project: Project, profile: Profile) {
        hideButtonsAndOffers()
        loadProjectInfo(project)
        loadClientAndClientActionsIfOwner(project, profile)
        loadSelectFreelancer(project, profile)
        loadActionsForFreelancersIfFreelancer(project, profile)
    }

    private fun loadSelectFreelancer(
        project: Project,
        profile: Profile
    ) {
        with(binding) {
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

    private fun loadActionsForFreelancersIfFreelancer(
        project: Project,
        profile: Profile
    ) {
        with(binding) {
            if (profile.activeRole == ActiveRole.FREELANCER && project.clientId != profile.id) {
                btnChatWithClient.visibility = View.VISIBLE
                btnSubmitOffer.visibility = View.VISIBLE
                btnSubmitOffer.setOnClickListener {
                    showSubmitOfferDialog(project.id, profile.id)
                }
            }
            val offerSubmitted = project.offers.firstOrNull { offer -> offer.freelancerId == profile.id }
            offerSubmitted?.let {
                showOfferDialog(offerSubmitted)
            }
        }
    }

    private fun loadClientAndClientActionsIfOwner(
        project: Project,
        profile: Profile
    ) {
        with(binding) {
            if (project.clientId == profile.id) {
                tvProjectClient.text = profile.name
                if (project.status == ProjectStatus.IN_PROGRESS) {
                    btnFinishProject.visibility = View.VISIBLE
                    btnFinishProject.setOnClickListener {
                        viewModel.finishProject(project.id)
                    }
                }
                btnViewClientProfile.visibility = View.GONE
                btnChatWithClient.visibility = View.GONE
                rvOffers.visibility = View.VISIBLE
                tvOffersLabel.visibility = View.VISIBLE
                setupAdapter(project.offers)
                if (project.offers.isEmpty()) {
                    tvOffersLabel.text = getString(R.string.no_offers)
                }
            } else {
                viewModel.getClientProfileInfo(project.clientId)
            }
        }
    }

    private fun loadProjectInfo(project: Project) {
        with(binding) {
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
        }
    }

    private fun hideButtonsAndOffers() {
        with(binding) {
            btnFinishProject.visibility = View.GONE
            btnChatWithClient.visibility = View.GONE
            btnSubmitOffer.visibility = View.GONE
            rvOffers.visibility = View.GONE
            tvOffersLabel.visibility = View.GONE
        }
    }

    private fun showSubmitOfferDialog(projectId: String, profileId: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_submit_offer)
            .setPositiveButton(getString(R.string.submit)) { dialog1, _ ->
                val dialog = dialog1 as Dialog
                try {
                    val offer = Offer(
                        freelancerId = profileId,
                        budget = dialog.findViewById<EditText>(R.id.etBudget).text.toString()
                            .toDouble(),
                        description = dialog.findViewById<EditText>(R.id.etDescription).text.toString(),
                        status = OfferStatus.PENDING
                    )
                    viewModel.submitOffer(offer, projectId)
                } catch (e: NumberFormatException) {
                    showSnackbar(getString(R.string.budget_must_be_number))
                }
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .create()
        dialog.show()
    }

    private fun setupAdapter(offers: List<Offer>) {
        val adapter = OffersAdapter(OffersActionsImpl())
        binding.rvOffers.adapter = adapter
        binding.rvOffers.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter.submitList(offers)
    }
}