package com.jorgetargz.projectseeker.framework.create_project

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.databinding.FragmentCreateProjectBinding
import com.jorgetargz.projectseeker.framework.common.BaseFragment
import com.jorgetargz.projectseeker.framework.common.Constants
import com.jorgetargz.projectseeker.framework.common.adapters.skills.SkillsActions
import com.jorgetargz.projectseeker.framework.common.adapters.skills.SkillsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@AndroidEntryPoint
class CreateProjectFragment : BaseFragment() {

    private val viewModel: CreateProjectViewModel by viewModels()
    private lateinit var binding: FragmentCreateProjectBinding
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
        setupAddSkillButton()
        setupDatePickers()
        setupCreateProjectButton()
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.projectCreated.collect { projectCreated ->
                    projectCreated?.let {
                        showSnackbar(getString(R.string.project_created))
                        viewModel.projectCreatedHandled()
                        findNavController().popBackStack()
                    }
                }
            }
        }
        observeLoading(viewModel)
        observeErrorString(viewModel)
        observeErrorResourceCode(viewModel)
    }

    private fun setupCreateProjectButton() {
        binding.createProjectButton.setOnClickListener {
            val title = binding.title.editText?.text.toString()
            val description = binding.description.editText?.text.toString()
            val startDate = binding.startDate.editText?.text.toString()
            val deadline = binding.deadlineDate.editText?.text.toString()
            val minBudget = binding.minBudget.editText?.text.toString()
            val maxBudget = binding.maxBudget.editText?.text.toString()
            val skills = adapter.currentList
            viewModel.createProject(
                    title,
                    description,
                    startDate,
                    deadline,
                    skills,
                    minBudget,
                    maxBudget
                )
        }
    }

    private fun setupDatePickers() {
        val showMaterialDatePickerListener: (TextInputLayout) -> Unit = { inputLayout ->
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(getString(R.string.date_picker_title))
                    .build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                val zoneId: ZoneId = ZoneId.systemDefault()
                val formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
                val selectedDate = Date(selection)
                val formattedDate = selectedDate.toInstant()
                    .atZone(zoneId)
                    .toLocalDate()
                    .format(formatter)
                inputLayout.editText?.setText(formattedDate)
            }
            datePicker.show(parentFragmentManager, null)
        }

        binding.btnChooseStartDate.setOnClickListener {
            showMaterialDatePickerListener(binding.startDate)
        }

        binding.btnChooseDeadlineDate.setOnClickListener {
            showMaterialDatePickerListener(binding.deadlineDate)
        }
    }

    private fun setupBinding() {
        binding = FragmentCreateProjectBinding.inflate(layoutInflater)
    }

    private fun setupAddSkillButton() {
        binding.addSkillButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_skill, binding.root, false)
            dialogView.findViewById<MaterialTextView>(R.id.tvRemminder).visibility = View.GONE

            MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton(getString(R.string.add)) { dialog1, _ ->
                    val dialog = dialog1 as Dialog
                    val skill = dialog.findViewById<TextInputEditText>(R.id.skill).text.toString().trim()
                    if (skill.isNotEmpty()) {
                        this.adapter.submitList(this.adapter.currentList + skill)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setOnCancelListener {
                    binding.root.removeView(dialogView)
                }
                .show()
        }
    }

    private fun setupSkillsAdapter() {
        adapter = SkillsAdapter(SkillsActionsImpl())
        binding.rvSkills.adapter = adapter
    }

}