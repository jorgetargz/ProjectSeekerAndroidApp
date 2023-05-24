package com.jorgetargz.projectseeker.framework.create_project

import androidx.lifecycle.viewModelScope
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.data.dto.projects.CreateProjectDTO
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.framework.common.Constants
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.lang.NumberFormatException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val createProjectDataManager: CreateProjectDataManager
) : BaseViewModel() {

    private val _projectCreated = MutableStateFlow<Project?>(null)
    val projectCreated: StateFlow<Project?> = _projectCreated

    fun createProject(
        title: String,
        description: String,
        startDate: String,
        deadline: String,
        skills: List<String>,
        minBudget: String,
        maxBudget: String
    ) {
        if (validateFields(title, description, startDate, deadline, skills, minBudget, maxBudget)) {
            viewModelScope.launch {
                val createProjectDTO = CreateProjectDTO(
                    title = title.trim(),
                    description = description.trim(),
                    startDate = LocalDate.parse(
                        startDate, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
                    ),
                    deadlineDate = LocalDate.parse(
                        deadline, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
                    ),
                    skills = skills.map { it.trim() },
                    minBudget = minBudget.toDouble(),
                    maxBudget = maxBudget.toDouble()
                )
                createProjectDataManager.createProject(createProjectDTO)
                    .catch {
                        _errorResourceCode.value = R.string.error_create_project
                    }.collect { result ->
                        when (result) {
                            is NetworkResult.Error -> {
                                handleNetworkError(result)
                            }

                            is NetworkResult.Loading -> _isLoading.value = true
                            is NetworkResult.Success -> {
                                result.data?.let { project ->
                                    _projectCreated.value = project
                                } ?: run {
                                    _errorResourceCode.value = R.string.error_create_project
                                }
                                _isLoading.value = false
                            }
                        }
                    }
            }
        }
    }

    private fun validateFields(
        title: String,
        description: String,
        startDate: String,
        deadline: String,
        skills: List<String>,
        minBudget: String,
        maxBudget: String
    ): Boolean {
        var isValid = true
        if (title.isEmpty() || description.isEmpty() || startDate.isEmpty() || deadline.isEmpty() || skills.isEmpty() || minBudget.isEmpty() || maxBudget.isEmpty()) {
            _errorResourceCode.value = R.string.error_empty_fields
            isValid = false
        }
        val datePattern: Pattern = Pattern.compile(Constants.REGEX_DATE)
        if (!datePattern.matcher(startDate).matches() || !datePattern.matcher(deadline).matches()) {
            _errorResourceCode.value = R.string.error_invalid_date
            isValid = false
        } else {
            val startDateParsed = LocalDate.parse(
                startDate, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
            )
            val deadlineParsed = LocalDate.parse(
                deadline, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT)
            )
            if (startDateParsed.isAfter(deadlineParsed) || startDateParsed.isEqual(deadlineParsed)
                || deadlineParsed.isBefore(LocalDate.now()) || startDateParsed.isBefore(LocalDate.now())
            ) {
                _errorResourceCode.value = R.string.error_invalid_date
                isValid = false
            }
        }
        try {
            if (minBudget.toDouble() > maxBudget.toDouble()) {
                _errorResourceCode.value = R.string.error_invalid_budget
                isValid = false
            }
        } catch (e: NumberFormatException) {
            _errorResourceCode.value = R.string.error_invalid_budget
            isValid = false
        }
        return isValid
    }

    fun projectCreatedHandled() {
        _projectCreated.value = null
    }
}
