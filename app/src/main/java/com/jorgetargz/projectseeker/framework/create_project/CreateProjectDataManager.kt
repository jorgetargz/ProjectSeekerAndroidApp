package com.jorgetargz.projectseeker.framework.create_project

import com.jorgetargz.projectseeker.data.ProjectsRepository
import com.jorgetargz.projectseeker.data.dto.projects.CreateProjectDTO
import javax.inject.Inject

class CreateProjectDataManager @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {
    fun createProject(createProjectDTO: CreateProjectDTO) =
        projectsRepository.createProject(createProjectDTO)
}