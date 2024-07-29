package se.infomaker.iap.action.open

import android.content.Context
import android.net.Uri
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import se.infomaker.iap.action.display.flow.mustachify
import se.infomaker.iap.action.display.openUri
import timber.log.Timber


object OpenActionHandler : ActionHandler {
    override fun canPerform(context: Context, operation: Operation): Boolean {
        return true
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        operation.parameters.optString("url", null)?.let { url ->
            try {
                val resolvedUrl = url.mustachify(operation.values)
                if (isPlayStoreUrl(resolvedUrl)) {
                    try {
                        val uri = Uri.parse(resolvedUrl)
                        uri.getQueryParameter("id")?.let { packageName ->
                            context.startActivity(context.packageManager.getLaunchIntentForPackage(packageName))
                            onResult.invoke(Result(true, operation.values, null))
                            return
                        }
                    } catch (e: Exception) {
                        Timber.d(e, "Failed to open app")
                    }
                }

                context.openUri(operation.moduleID ?: "global", resolvedUrl, operation.values)
                onResult.invoke(Result(true, operation.values, null))
            } catch (t: Throwable) {
                Timber.e(t, "Failed to open uri")
                onResult.invoke(Result(false, operation.values, t.localizedMessage))
            }
        }
    }

    private fun isPlayStoreUrl(resolvedUrl: String) =
            resolvedUrl.startsWith("https://play.google.com/store/apps/details")
}