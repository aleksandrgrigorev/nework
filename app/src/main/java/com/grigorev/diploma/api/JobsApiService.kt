package com.grigorev.diploma.api

import com.grigorev.diploma.dto.Job
import retrofit2.Response
import retrofit2.http.*

interface JobsApiService {

    @GET("/api/{userId}/jobs")
    suspend fun getJobsById(@Path("userId")authorId: Int) : Response<List<Job>>

    @POST("/api/my/jobs")
    suspend fun saveMyJob(@Body job: Job): Response<Job>

    @DELETE("/api/my/jobs/{id}")
    suspend fun removeMyJobById(@Path("id") id: Int): Response<Unit>
}