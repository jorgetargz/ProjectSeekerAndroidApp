package com.jorgetargz.projectseeker.network

import com.jorgetargz.projectseeker.data.dto.projects.AssignProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.CreateProjectDTO
import com.jorgetargz.projectseeker.data.dto.projects.ProjectInfoDTO
import com.jorgetargz.projectseeker.data.dto.projects.SubmitOfferDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProjectServices {

    @POST("secured/projects/client/createProject")
    suspend fun createProject(@Body createProjectDTO: CreateProjectDTO) : Response<ProjectInfoDTO>

    @GET("secured/projects/openProjects")
    suspend fun getOpenProjects() : Response<List<ProjectInfoDTO>>

    @GET("secured/projects/client/myProjects")
    suspend fun getMyProjects() : Response<List<ProjectInfoDTO>>

    @GET("secured/projects/projectInfo")
    suspend fun getProjectInfo(@Query("id") projectId : String ) : Response<ProjectInfoDTO>

    @POST("secured/projects/client/assignFreelancer")
    suspend fun assignFreelancer(@Body assignProjectDTO: AssignProjectDTO) : Response<ProjectInfoDTO>

    @POST("secured/projects/freelancer/submitOffer")
    suspend fun submitOffer(@Body submitOfferDTO: SubmitOfferDTO) : Response<ProjectInfoDTO>

    @POST("secured/projects/client/finishProject")
    suspend fun finishProject(@Query("id") projectId : String ) : Response<ProjectInfoDTO>
}