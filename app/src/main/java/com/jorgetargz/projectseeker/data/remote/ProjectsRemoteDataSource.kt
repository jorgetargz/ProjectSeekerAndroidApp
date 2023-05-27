package com.jorgetargz.projectseeker.data.remote

import com.jorgetargz.projectseeker.data.dto.projects.AssignProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.CreateProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.SubmitOfferDTO
import com.jorgetargz.projectseeker.network.ProjectServices
import javax.inject.Inject

class ProjectsRemoteDataSource @Inject constructor(
    private val projectsService: ProjectServices,
    private val safeSpringApiCall: SafeSpringApiCall
) {

    suspend fun createProject(createProjectDTO: CreateProjectDTO) =
        safeSpringApiCall.safeApiCall { projectsService.createProject(createProjectDTO) }

    suspend fun getOpenProjects() =
        safeSpringApiCall.safeApiCall { projectsService.getOpenProjects() }

    suspend fun getMyProjects() =
        safeSpringApiCall.safeApiCall { projectsService.getMyProjects() }

    suspend fun getMyOpenProjects() =
        safeSpringApiCall.safeApiCall { projectsService.getMyOpenProjects() }

    suspend fun getMyInProgressProjects() =
        safeSpringApiCall.safeApiCall { projectsService.getMyInProgressProjects() }

    suspend fun getProjectsAssignedToMe() =
        safeSpringApiCall.safeApiCall { projectsService.getProjectsAssignedToMe() }

    suspend fun getProjectsWhereIHaveOffer() =
        safeSpringApiCall.safeApiCall { projectsService.getProjectsWhereIHaveOffer() }

    suspend fun getOpenProjectsBySkills(skills: List<String>) =
        safeSpringApiCall.safeApiCall { projectsService.getOpenProjectsBySkills(skills) }

    suspend fun getProjectInfo(projectId: String) =
        safeSpringApiCall.safeApiCall { projectsService.getProjectInfo(projectId) }

    suspend fun assignFreelancer(assignProjectDTO: AssignProjectDTO) =
        safeSpringApiCall.safeApiCall { projectsService.assignFreelancer(assignProjectDTO) }

    suspend fun submitOffer(submitOfferDTO: SubmitOfferDTO) =
        safeSpringApiCall.safeApiCall { projectsService.submitOffer(submitOfferDTO) }

    suspend fun finishProject(projectId: String) =
        safeSpringApiCall.safeApiCall { projectsService.finishProject(projectId) }

}