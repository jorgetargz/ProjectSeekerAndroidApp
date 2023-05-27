package com.jorgetargz.projectseeker.data

import com.jorgetargz.projectseeker.data.dto.projects.AssignProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.CreateProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.SubmitOfferDTO
import com.jorgetargz.projectseeker.data.mapper.toProject
import com.jorgetargz.projectseeker.data.remote.ProjectsRemoteDataSource
import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProjectsRepository @Inject constructor(
    private val projectsRemoteDataSource: ProjectsRemoteDataSource
) {

    fun createProject(createProjectDTO: CreateProjectDTO) : Flow<NetworkResult<Project>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.createProject(createProjectDTO)
            emit(result.map { response -> response.toProject() })
        }
    }

    fun getOpenProjects() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getOpenProjects()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getMyProjects() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getMyProjects()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getMyOpenProjects() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getMyOpenProjects()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getMyInProgressProjects() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getMyInProgressProjects()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getProjectsAssignedToMe() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getProjectsAssignedToMe()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getProjectsWhereIHaveOffer() : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getProjectsWhereIHaveOffer()
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getOpenProjectsBySkills(skills: List<String>) : Flow<NetworkResult<List<Project>>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getOpenProjectsBySkills(skills)
            emit(result.map { response -> response.map { it.toProject() } })
        }
    }

    fun getProjectInfo(projectId: String) : Flow<NetworkResult<Project>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.getProjectInfo(projectId)
            emit(result.map { response -> response.toProject() })
        }
    }

    fun assignFreelancer(assignProjectDTO: AssignProjectDTO) : Flow<NetworkResult<Project>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.assignFreelancer(assignProjectDTO)
            emit(result.map { response -> response.toProject() })
        }
    }

    fun submitOffer(submitOfferDTO: SubmitOfferDTO) : Flow<NetworkResult<Project>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.submitOffer(submitOfferDTO)
            emit(result.map { response -> response.toProject() })
        }
    }

    fun finishProject(projectId: String) : Flow<NetworkResult<Project>> {
        return flow {
            emit(NetworkResult.Loading())
            val result = projectsRemoteDataSource.finishProject(projectId)
            emit(result.map { response -> response.toProject() })
        }
    }
}
