package com.example.ktshows.domain.UseCase

import com.example.ktshows.data.remote.VideoData
import com.example.ktshows.domain.Repository.KtShowsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetKtShowsDataUseCase @Inject constructor(private val videoRepository: KtShowsRepository) {

    suspend fun execute(): Flow<List<VideoData>> = videoRepository.getVideos()
}