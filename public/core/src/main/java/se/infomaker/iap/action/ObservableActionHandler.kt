package se.infomaker.iap.action

import android.content.Context
import io.reactivex.Observable

interface ObservableActionHandler : ActionHandler {

    /**
     * Returns an observable that emits each time the condition of the [ActionHandler]s ability
     * to [perform] changes.
     *
     * Emitted value reflects what [canPerform] would return.
     */
    fun observeCanPerform(context: Context, operation: Operation): Observable<Boolean>
}