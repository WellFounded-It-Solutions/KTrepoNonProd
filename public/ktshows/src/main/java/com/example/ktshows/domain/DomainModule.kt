package com.example.ktshows.domain

import com.example.ktshows.data.remote.ProductApi
import com.example.ktshows.domain.Repository.KtShowsRepository
import com.example.ktshows.domain.Repository.KtShowsRepositoryImpl
import com.example.ktshows.domain.UseCase.GetKtShowsDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import se.infomaker.frt.ui.fragment.KtShowsViewModel

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideKtShowsRepository(apiService: ProductApi): KtShowsRepository {
        return KtShowsRepositoryImpl(apiService)
    }

    @Provides
    fun provideGetKtShowsDataUseCase(ktShowsRepository: KtShowsRepository): GetKtShowsDataUseCase {
        return GetKtShowsDataUseCase(ktShowsRepository)
    }


    @Provides
    fun provideKtShowsViewModel(
        getVideoDataUseCase: GetKtShowsDataUseCase
    ): KtShowsViewModel {
        return KtShowsViewModel(getVideoDataUseCase)
    }

}