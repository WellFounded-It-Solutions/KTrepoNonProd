package se.infomaker.frt.moduleinterface.deeplink

import android.net.Uri

class OpenAppUriTargetResolver : UriTargetResolver {

    override fun resolve(uri: Uri): UriTarget {
        if (uri.lastPathSegment == null && uri.getBooleanQueryParameter("openinapp", false)) {
            return UriTarget("shared", null)
        }
        return UriTarget.NOT_FOUND
    }
}