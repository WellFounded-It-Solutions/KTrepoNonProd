package com.example.ktshows.data.remote

import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface ProductApi {

        @GET("home/gen_videos/Features")
        suspend fun getFeatures(): Response<List<VideoData>>

    }