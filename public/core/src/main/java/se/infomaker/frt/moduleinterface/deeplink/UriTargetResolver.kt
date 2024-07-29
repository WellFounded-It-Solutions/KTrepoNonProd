package se.infomaker.frt.moduleinterface.deeplink

import android.net.Uri

interface UriTargetResolver {
    fun resolve(uri: Uri) : UriTarget?
}
