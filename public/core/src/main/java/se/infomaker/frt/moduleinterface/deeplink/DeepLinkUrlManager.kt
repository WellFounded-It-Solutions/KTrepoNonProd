package se.infomaker.frt.moduleinterface.deeplink

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.UUID


object DeepLinkUrlManager {
    fun handle(context: Context, targetHandler: UriTargetHandler, uri: Uri, onComplete: () -> Unit) {
        var onDone: (() -> Unit)? = null
        val disposable = Observable.fromIterable(handlers)
                .observeOn(Schedulers.newThread())
                .map { it.resolve(uri) ?: UriTarget.NOT_FOUND }
                .filter { it != UriTarget.NOT_FOUND }
                .subscribeOn(AndroidSchedulers.mainThread())
                .firstOrError()
                .subscribe { target, _ ->
                    target?.let {
                        targetHandler.open(it)
                    } ?: {
                        openExternally(context, uri)
                    }.invoke()

                    onDone?.invoke() ?: {
                        Timber.w("Failed to create on done lambda")
                        onComplete()
                    }.invoke()
                }
        onDone = {
            disposable.dispose()
            onComplete.invoke()
            Timber.d("Handed deep link: $uri")
        }
    }

    fun register(resolver: UriTargetResolver): Boolean {
        return handlers.add(resolver)
    }

    private fun openExternally(context: Context, uri: Uri) {
        val genericUri = Uri.parse("${uri.scheme}://${UUID.randomUUID()}.com")

        context.packageManager.resolveActivity(Intent(Intent.ACTION_VIEW, genericUri), MATCH_DEFAULT_ONLY)?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
            context.startActivity(intent)
        } ?: run {
            Timber.e("Could not resolve activity to open uri: $genericUri")
        }
    }

    private val handlers = mutableSetOf<UriTargetResolver>()
}
