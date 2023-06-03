package com.jorgetargz.projectseeker.framework.list_projects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorgetargz.projectseeker.databinding.FragmentListProjectsBinding
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import com.jorgetargz.projectseeker.framework.common.adapters.projects.ProjectsActions
import com.jorgetargz.projectseeker.framework.common.adapters.projects.ProjectsAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ListProjectsFragment : BaseFragment() {

    private val viewModel: ListProjectsViewModel by viewModels()
    private val binding: FragmentListProjectsBinding by lazy {
        FragmentListProjectsBinding.inflate(layoutInflater)
    }
    private lateinit var viewerProfile: Profile
    private lateinit var adapter: ProjectsAdapter

    inner class ProjectsActionsImpl : ProjectsActions {
        override fun onClick(projectId: String) {
            val action =
                ListProjectsFragmentDirections.actionListProjectsFragmentToViewProjectFragment(
                    projectId
                )
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupAddProjectFAB()
        observeViewModel()
        viewModel.getMyProfile()
        return binding.root
    }

    private fun setupClientFilterButtons() {
        with(binding) {
            listMyOpenProjectsButton.setOnClickListener {
                viewModel.loadMyOpenProjects()
            }
            listMyProjectsInProgressButton.setOnClickListener {
                viewModel.loadMyProjectsInProgress()
            }
            listMyProjectsButton.setOnClickListener {
                viewModel.loadMyProjects()
            }
            listAllOpenProjectsButton.setOnClickListener {
                viewModel.loadOpenProjects()
            }
        }
    }

    private fun setupFreelancerFilterButtons(profile: Profile.Freelancer) {
        with(binding) {
            listProjectsAssignedToMeButton.setOnClickListener {
                viewModel.loadProjectsAssignedToMe()
            }
            listProjectsWhereIHaveOfferFreelancerButton.setOnClickListener {
                viewModel.loadProjectsWhereIHaveOfferFreelancer()
            }
            listOpenProjectsMatchingMySkillsButton.setOnClickListener {
                viewModel.loadOpenProjectsMatchingMySkills(profile.skills)
            }
            listAllOpenProjectsButton.setOnClickListener {
                viewModel.loadOpenProjects()
            }
        }
    }

    private fun setupAddProjectFAB() {
        binding.addProjectFloatingActionButton.setOnClickListener {
            val action =
                ListProjectsFragmentDirections.actionListProjectsFragmentToCreatProjectFragment()
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        observeStateFlowOnStarted {
            viewModel.profile.collect { profile ->
                Timber.d("Profile: $profile")
                profile?.let {
                    setupContent(it)
                    viewerProfile = profile
                }
            }
        }
        observeStateFlowOnStarted {
            viewModel.projects.collect { projects ->
                projects?.let {
                    if (viewerProfile.id.isNotEmpty() && viewerProfile.activeRole == ActiveRole.FREELANCER) {
                        adapter.submitList(it.filter { project ->
                            !project.clientId.contains(viewerProfile.id)
                        })
                    } else {
                        adapter.submitList(it)
                    }
                }
            }
        }
        observeLoading(viewModel.isLoading)
        observeErrorString(viewModel.errorString) { viewModel.errorStringHandled() }
        observeErrorResourceCode(viewModel.errorResourceCode) { viewModel.errorResourceCodeHandled() }
    }

    private fun setupContent(profile: Profile) {
        with(binding) {
            when (profile) {
                is Profile.Client -> {
                    viewModel.loadMyProjects()
                    setupClientFilterButtons()
                    addProjectFloatingActionButton.visibility = View.VISIBLE
                }

                is Profile.Freelancer -> {
                    viewModel.loadOpenProjects()
                    setupFreelancerFilterButtons(profile)
                    addProjectFloatingActionButton.visibility = View.GONE
                }
            }
        }
        setupAdapter(profile)
        setupDisplayFilterButtonsFab(profile)
    }

    private fun setupDisplayFilterButtonsFab(profile: Profile) {
        hideAllFilterButtons()
        with(binding) {
            when (profile) {
                is Profile.Client -> {
                    var showFilters = false
                    displayFilterButtonsFab.setOnClickListener {
                        if (!showFilters) {
                            showFilters = true
                            showClientFilterButtons()
                        } else {
                            showFilters = false
                            hideAllFilterButtons()
                        }
                    }
                }

                is Profile.Freelancer -> {
                    var showFilters = false
                    displayFilterButtonsFab.setOnClickListener {
                        if (!showFilters) {
                            showFilters = true
                            showFreelancerFilterButtons()
                        } else {
                            showFilters = false
                            hideAllFilterButtons()
                        }
                    }
                }
            }
        }
    }

    private fun hideAllFilterButtons() {
        with(binding) {
            listAllOpenProjectsButton.visibility = View.GONE
            listMyProjectsButton.visibility = View.GONE
            listMyOpenProjectsButton.visibility = View.GONE
            listMyProjectsInProgressButton.visibility = View.GONE
            listProjectsAssignedToMeButton.visibility = View.GONE
            listProjectsWhereIHaveOfferFreelancerButton.visibility = View.GONE
            listOpenProjectsMatchingMySkillsButton.visibility = View.GONE
        }
    }

    private fun showFreelancerFilterButtons() {
        with(binding) {
            listAllOpenProjectsButton.visibility = View.VISIBLE
            listMyProjectsButton.visibility = View.GONE
            listMyOpenProjectsButton.visibility = View.GONE
            listMyProjectsInProgressButton.visibility = View.GONE
            listProjectsAssignedToMeButton.visibility = View.VISIBLE
            listProjectsWhereIHaveOfferFreelancerButton.visibility = View.VISIBLE
            listOpenProjectsMatchingMySkillsButton.visibility = View.VISIBLE
        }
    }

    private fun showClientFilterButtons() {
        with(binding) {
            listAllOpenProjectsButton.visibility = View.VISIBLE
            listMyProjectsButton.visibility = View.VISIBLE
            listMyOpenProjectsButton.visibility = View.VISIBLE
            listMyProjectsInProgressButton.visibility = View.VISIBLE
            listProjectsAssignedToMeButton.visibility = View.GONE
            listProjectsWhereIHaveOfferFreelancerButton.visibility = View.GONE
            listOpenProjectsMatchingMySkillsButton.visibility = View.GONE
        }
    }

    private fun setupAdapter(profile: Profile) {
        adapter = ProjectsAdapter(profile, ProjectsActionsImpl())
        binding.listProjectsRecyclerView.adapter = adapter
        binding.listProjectsRecyclerView.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
    }


}