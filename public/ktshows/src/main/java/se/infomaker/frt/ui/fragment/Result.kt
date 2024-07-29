package se.infomaker.frt.ui.fragment

sealed class UIResult<out T> {
    data class Success<out T>(val data: T) : UIResult<T>()
    data class Error(val exception: Throwable) : UIResult<Nothing>()
    object Loading : UIResult<Nothing>()
}