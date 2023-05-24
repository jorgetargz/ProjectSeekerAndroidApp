package com.jorgetargz.projectseeker.framework.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentProfileBinding
import com.jorgetargz.projectseeker.domain.user.ActiveRole
import com.jorgetargz.projectseeker.domain.user.Availability
import com.jorgetargz.projectseeker.domain.user.Profile
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import com.jorgetargz.projectseeker.framework.common.adapters.skills.SkillsActions
import com.jorgetargz.projectseeker.framework.common.adapters.skills.SkillsAdapter
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.avatar.AvatarView
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var binding: FragmentProfileBinding

    private val availabilityOptions = arrayListOf(
        Availability.FULL_TIME,
        Availability.PART_TIME,
        Availability.UNAVAILABLE
    )

    private val activeRoleOptions = arrayListOf(
        ActiveRole.CLIENT,
        ActiveRole.FREELANCER
    )

    private val imgResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {viewModel.updateProfilePic(it) }
        } else {
            showSnackbar("Image not updated")
        }
    }

    private lateinit var adapter: SkillsAdapter

    inner class SkillsActionsImpl : SkillsActions {
        override fun onLongClick(skill: String) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_skill))
                .setMessage(getString(R.string.delete_skill_message))
                .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                    val skills = adapter.currentList.toMutableList()
                    skills.remove(skill)
                    adapter.submitList(skills)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupBinding()
        setupSkillsAdapter()
        setupAvailabilitySpinner()
        setupUpdateProfileButton()
        observeViewModel()
        viewModel.getMyProfile()
        return binding.root
    }

    private fun setupUpdateProfileButton() {
        binding.updateProfileButton.setOnClickListener {
            val title = binding.titleEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            if (binding.activeRole.selectedItem == ActiveRole.FREELANCER) {
                val availability = binding.availability.selectedItem as Availability
                val skills = adapter.currentList
                viewModel.updateFreelancerProfile(availability, skills, title, description)
            } else if (binding.activeRole.selectedItem == ActiveRole.CLIENT) {
                viewModel.updateClientProfile(title, description)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profile.collect { profile ->
                    Timber.d("Profile: $profile")
                    profile?.let { displayProfile(it) }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatClientProfile.collect { user ->
                    user?.let {
                        binding.avatarView.setUserData(user)
                        val navigationView = requireActivity().findViewById<NavigationView>(R.id.nav_view)
                        val headerView = navigationView.getHeaderView(0)
                        val headerAvatar = headerView.findViewById<AvatarView>(R.id.avatarView)
                        headerAvatar.setUserData(user)
                    }
                }
            }
        }
        observeLoading(viewModel)
        observeErrorString(viewModel)
        observeErrorResourceCode(viewModel)
    }

    private fun displayProfile(profile: Profile) {
        with(binding) {
            when (profile) {
                is Profile.Client -> {
                    availability.visibility = View.GONE
                    availabilityLabel.visibility = View.GONE
                    skills.visibility = View.GONE
                    skillsLabel.visibility = View.GONE
                    addSkillButton.visibility = View.GONE
                }

                is Profile.Freelancer -> {
                    availability.setSelection(availabilityOptions.indexOf(profile.availability))
                    adapter.submitList(profile.skills)
                    setupAddSkillButton()

                }
            }
            setupProfilePicture()
            setupActiveRoleSpinner(profile)
            name.text = profile.name
            email.text = profile.email
            if (profile.phone.isEmpty()) {
                phoneLabel.visibility = View.GONE
                phone.visibility = View.GONE
            } else {
                phoneLabel.visibility = View.VISIBLE
                phone.visibility = View.VISIBLE
            }
            phone.text = profile.phone
            titleEditText.setText(profile.title)
            descriptionEditText.setText(profile.description)
        }
    }

    private fun setupAddSkillButton() {
        binding.addSkillButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setView(R.layout.dialog_add_skill)
                .setPositiveButton(getString(R.string.add)) { dialog1, _ ->
                    val dialog = dialog1 as Dialog
                    val skill =
                        dialog.findViewById<TextInputEditText>(R.id.skill).text.toString().trim()
                    if (skill.isNotEmpty()) {
                        this.adapter.submitList(this.adapter.currentList + skill)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun setupSkillsAdapter() {
        adapter = SkillsAdapter(SkillsActionsImpl())
        binding.skills.adapter = adapter
    }

    private fun setupProfilePicture() {
        val streamUser = ChatClient.instance().getCurrentUser()
        streamUser?.let { binding.avatarView.setUserData(streamUser) }
        binding.avatarView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            imgResult.launch(intent)
        }
    }

    private fun setupActiveRoleSpinner(profile: Profile) {
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, activeRoleOptions)
        binding.activeRole.adapter = adapter
        binding.activeRole.setSelection(activeRoleOptions.indexOf(profile.activeRole))

        binding.activeRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                //When selected Client -> hide availability and skills
                //When selected Freelancer -> show availability and skills
                if (position == 0) {
                    binding.availability.visibility = View.GONE
                    binding.availabilityLabel.visibility = View.GONE
                    binding.skills.visibility = View.GONE
                    binding.skillsLabel.visibility = View.GONE
                    binding.addSkillButton.visibility = View.GONE
                    binding.updateProfileButton.setText(R.string.update_profile_client)
                } else {
                    binding.availability.visibility = View.VISIBLE
                    binding.availabilityLabel.visibility = View.VISIBLE
                    binding.skills.visibility = View.VISIBLE
                    binding.skillsLabel.visibility = View.VISIBLE
                    binding.addSkillButton.visibility = View.VISIBLE
                    binding.updateProfileButton.setText(R.string.update_profile_freelancer)
                }
                if (profile.activeRole != activeRoleOptions[position]) {
                    viewModel.setActiveRole(activeRoleOptions[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //Do nothing
            }
        }
    }

    private fun setupAvailabilitySpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, availabilityOptions)
        binding.availability.adapter = adapter
    }

    private fun setupBinding() {
        binding = FragmentProfileBinding.inflate(layoutInflater)
    }
}