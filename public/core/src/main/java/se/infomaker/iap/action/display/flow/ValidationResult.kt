package se.infomaker.iap.action.display.flow

import io.reactivex.Single


data class ValidationResult(val success: Boolean, val message: String?) {
    companion object {
        val SUCCESS = ValidationResult(true, null)
        val SINGLE_SUCCESS : Single<ValidationResult> get() = Single.just(SUCCESS)
    }
}