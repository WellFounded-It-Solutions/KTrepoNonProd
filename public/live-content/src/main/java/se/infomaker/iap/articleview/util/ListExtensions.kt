package se.infomaker.iap.articleview.util

fun <T> List<T>.containsAny(values: List<T>) = values.any { contains(it) }