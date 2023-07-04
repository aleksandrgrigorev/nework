package com.grigorev.diploma.api

import com.grigorev.diploma.dto.Event
import com.grigorev.diploma.dto.Media
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface EventApiService {

    @GET("/api/events/{id}/before")
    suspend fun getEventBefore(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/{id}/after")
    suspend fun getEventAfter(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Event>>

    @GET("/api/events/latest")
    suspend fun getEventLatest(@Query("count") count: Int): Response<List<Event>>

    @POST("/api/events")
    suspend fun saveEvent(@Body event: Event): Response<Event>

    @DELETE("/api/events/{id}")
    suspend fun removeById(@Path("id") id: Int): Response<Unit>

    @POST("/api/events/{id}/likes")
    suspend fun likeById(@Path("id") id: Int): Response<Event>

    @DELETE("/api/events/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Int): Response<Event>

    @POST("/api/events/{id}/participants")
    suspend fun participate(@Path("id") id: Int): Response<Event>

    @DELETE("/api/events/{id}/participants")
    suspend fun doNotParticipate(@Path("id") id: Int): Response<Event>

    @Multipart
    @POST("/api/media")
    suspend fun uploadMedia(@Part media: MultipartBody.Part): Response<Media>
}