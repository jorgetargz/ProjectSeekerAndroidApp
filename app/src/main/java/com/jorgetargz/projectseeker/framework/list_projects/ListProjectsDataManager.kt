package com.jorgetargz.projectseeker.framework.list_projects

import com.jorgetargz.projectseeker.data.ProjectsRepository
import com.jorgetargz.projectseeker.data.UsersRepository
import javax.inject.Inject

class ListProjectsDataManager @Inject constructor(
    private val usersRepository: UsersRepository,
    private val projectsRepository: ProjectsRepository
) {
    fun getMyProfile() = usersRepository.getMyProfile()
    fun getOpenProjects() = projectsRepository.getOpenProjects()
    fun getMyProjects() = projectsRepository.getMyProjects()
}