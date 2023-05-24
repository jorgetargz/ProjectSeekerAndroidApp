package com.jorgetargz.projectseeker.framework.view_project

import com.jorgetargz.projectseeker.domain.project.Project
import com.jorgetargz.projectseeker.domain.user.Profile

data class ViewProjectState(
    val project: Project? = null,
    val viewerProfile: Profile? = null,
    val assignedFreelancer: Profile? = null,
    val clientProfile : Profile? = null,
)