package com.jorgetargz.projectseeker.framework.list_projects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jorgetargz.projectseeker.databinding.FragmentListProjectsBinding
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import com.jorgetargz.projectseeker.framework.common.adapters.projects.ProjectsActions
import com.jorgetargz.projectseeker.framework.common.adapters.projects.ProjectsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ListProjectsFragment : BaseFragment() {

    private val viewModel: ListProjectsViewModel by viewModels()
    private lateinit var binding: FragmentListProjectsBinding
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
        setupBindng()
        setupAddProjectFAB()
        observeViewModel()
        viewModel.getMyProfile()
        return binding.root
    }

    private fun setupAddProjectFAB() {
        binding.addProjectFloatingActionButton.setOnClickListener {
            val action =
                ListProjectsFragmentDirections.actionListProjectsFragmentToCreatProjectFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupBindng() {
        binding = FragmentListProjectsBinding.inflate(layoutInflater)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    Timber.d("Profile: $profile")
                    profile?.let {
                        setupContent(it)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.projects.collect { projects ->
                    projects?.let { adapter.submitList(it) }
                }
            }
        }
        observeLoading(viewModel)
        observeErrorString(viewModel)
        observeErrorResourceCode(viewModel)
    }

    private fun setupContent(profile: Profile) {
        when (profile) {
            is Profile.Client -> {
                binding.addProjectFloatingActionButton.visibility = View.VISIBLE
                viewModel.loadMyProjects()
            }

            is Profile.Freelancer -> {
                binding.addProjectFloatingActionButton.visibility = View.GONE
                viewModel.loadOpenProjects()
            }
        }
        setupAdapter(profile)
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