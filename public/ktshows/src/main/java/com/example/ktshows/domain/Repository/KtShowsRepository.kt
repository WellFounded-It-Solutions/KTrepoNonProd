package com.example.ktshows.domain.Repository

import com.example.ktshows.data.remote.VideoData
import kotlinx.coroutines.flow.Flow

interface KtShowsRepository {
        suspend fun getVideos(): Flow<List<VideoData>>

}