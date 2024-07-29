package se.infomaker.iap.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.navigaglobal.mobile.R
import io.reactivex.Observable
import timber.log.Timber
import java.util.HashMap


object ActionManager : ActionHandler {

    private val groupHandlers = HashMap<String, ActionHandler>()
    private val handlers = HashMap<String, ActionHandler>()

    init {
        handlers["tel"] = PhoneActionHandler
    }
    fun registerGroupHandler(prefix: String, handler: ActionHandler) {
        groupHandlers[prefix] = handler
    }

    fun unregisterGroupHandler(prefix:String, handler: ActionHandler) {
        groupHandlers[prefix]?.takeIf { it == handler }.let {
            handlers.remove(prefix)
        }
    }

    fun register(action: String, handler: ActionHandler) {
        handlers[action] = handler
    }

    fun unregister(action:String, handler: ActionHandler) {
        handlers[action]?.takeIf { it == handler }.let {
            handlers.remove(action)
        }
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        Timber.d("Performing operation: %s", operation)
        groupHandlers.filter { (prefix, actionHandler) -> operation.action.startsWith(prefix) && actionHandler.canPerform(context, operation) }.map { it.value }.firstOrNull()?.let {
            it.perform(context, operation, onResult)
            return
        }

        val handler = handlers[operation.action]
        if (handler == null) {
            onResult.invoke(Result(success = false, errorMessage = "No handler for ${operation.action}"))
        } else {
            if (handler.isLongRunning()) {
                val resultHandler = ProgressResultHandler(context, onResult)
                resultHandler.start()
                handler.perform(context, operation) {
                    resultHandler.onComplete(it)
                }
            } else {
                handler.perform(context, operation, onResult)
            }
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return actionHandler(context, operation)?.canPerform(context, operation) ?: false
    }

    fun observeCanPerform(context: Context, operation: Operation): Observable<Boolean>? {
        return (actionHandler(context, operation) as? ObservableActionHandler)?.observeCanPerform(context, operation)
    }

    private fun actionHandler(context: Context, operation: Operation): ActionHandler? {
        groupHandlers.filter { (prefix, actionHandler) -> operation.action.startsWith(prefix) && actionHandler.canPerform(context, operation) }.map { it.value }.firstOrNull()?.let {
            return it
        }
        return handlers[operation.action]
    }
}

object PhoneActionHandler : ActionHandler {
    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", operation.getParameter("number"), null))
        try {
            context.startActivity(intent)
        }
        catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.no_phone_app), Toast.LENGTH_SHORT).show()
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return operation.getParameter("number") != null
    }

}
