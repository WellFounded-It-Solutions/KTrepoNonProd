package se.infomaker.iap.articleview.util

interface Provider<T> {
    fun provide(): T
}