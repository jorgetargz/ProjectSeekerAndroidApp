package com.jorgetargz.projectseeker.framework.view_project

import com.jorgetargz.projectseeker.data.ProjectsRepository
import com.jorgetargz.projectseeker.data.UsersRepository
import com.jorgetargz.projectseeker.data.dto.projects.AssignProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.SubmitOfferDTO
import javax.inject.Inject

class ViewProjectDataManager @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val usersRepository: UsersRepository
) {
    fun getProjectInfo(projectId: String) = projectsRepository.getProjectInfo(projectId)

    fun getMyProfile() = usersRepository.getMyProfile()

    fun getProfile(userId: String) = usersRepository.getProfile(userId)

    fun assignFreelancer(assignProjectDTO: AssignProjectDTO) =
        projectsRepository.assignFreelancer(assignProjectDTO)

    fun submitOffer(submitOfferDTO: SubmitOfferDTO) =
        projectsRepository.submitOffer(submitOfferDTO)

    fun finishProject(projectId: String) = projectsRepository.finishProject(projectId)
}