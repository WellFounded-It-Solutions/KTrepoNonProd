package se.infomaker.iap.appreview

data class AppReviewConfig(val appReview: AppReview?)

data class AppReview (val enabled:Boolean?, val feedbackEmail: String?, val snoozeInterval:Int?, val minimumUsageTime:Int?, val debug: Boolean?)