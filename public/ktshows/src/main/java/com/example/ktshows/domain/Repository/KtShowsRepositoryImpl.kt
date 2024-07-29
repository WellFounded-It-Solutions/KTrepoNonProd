package com.example.ktshows.domain.Repository

import com.example.ktshows.data.remote.ProductApi
import com.example.ktshows.data.remote.VideoData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KtShowsRepositoryImpl @Inject constructor(
    private val apiService: ProductApi
) : KtShowsRepository {


    override suspend fun getVideos(): Flow<List<VideoData>> = flow {
       emit (apiService.getFeatures().body() ?: emptyList())
    }

}