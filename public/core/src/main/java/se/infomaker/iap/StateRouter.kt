package se.infomaker.iap

import android.content.Context
import android.content.Intent


/**
 * Move the application of the springboard with the current state
 */
interface StateRouter {

    /**
     * Routes the application to a usable state after the entire initialization process is completed
     * and the app is ready for user interaction.
     *
     * Any asynchronous work that happens as a result of this routing is the responsibility of the
     * [StateRouter] and must be accessible by a call to [currentRoute].
     *
     * To avoid loosing state, an [Intent] is passed ([intent]). Use this to get a hold of state
     * that the app is started with. An example of this would be push "deep linking".
     *
     * By providing an [onComplete] the caller is given the opportunity to perform any necessary
     * clean up before the application is routed to a usable state. This is especially important for
     * an [android.app.Activity] to [android.app.Activity.finish] to avoid a weird back stack or
     * leak memory.
     *
     * A route might not complete. [onCancel] is run if a route is cancelled early instead of
     * completing.
     */
    fun route(context: Context, intent: Intent, onComplete: () ->  Unit = {}, onCancel: () -> Unit = {})

    /**
     * The Route that is currently active.
     *
     * It is the responsibility of each [StateRouter] to properly report the current [Route] in
     * order to be able to [Route.cancel] it at any point to abort routing.
     */
    fun currentRoute(): Route?
}


/**
 * A [Route] is responsible for any asynchronous work that happens as a result of a
 * [StateRouter.route]. That responsibility includes cancellation of ongoing routing
 * in favor of whatever the caller intends to run instead.
 *
 * It is the responsibility of the [Route] to run any onCancel lambda passed [StateRouter.route]
 * to the when cancelled by a call to [Route.cancel].
 */
abstract class Route : Cancellable


/**
 * Marker interface to indicate that the given component is able to handle being cancelled.
 */
interface Cancellable {

    /**
     * Cancel any ongoing operations and prevent this component from further execution.
     */
    fun cancel()
}