package se.infomaker.iap.appreview

import se.infomaker.iap.appreview.repository.AppReviewRepository

object AppReviewRepositoryProvider {

    private var appReviewRepository: AppReviewRepository? = null

    fun configure(appReviewRepository: AppReviewRepository) {
        this.appReviewRepository = appReviewRepository
    }

    fun provide(): AppReviewRepository {
        return appReviewRepository ?: throw NullPointerException("The AppRepository must be defined before use.")
    }
}