package se.infomaker.iap.action.display

import android.content.Context
import androidx.fragment.app.Fragment
import se.infomaker.iap.action.ActionHandler
import se.infomaker.iap.action.Operation
import se.infomaker.iap.action.Result
import java.util.HashMap

object DisplayManager : ActionHandler, DisplayProvider {
    override fun create(operation: Operation): androidx.fragment.app.Fragment {
        val fragment = providers[operation.action]?.create(operation)
        if (fragment != null) {
            return fragment
        }
        return ErrorFragment()
    }

    private val providers = HashMap<String, DisplayProvider>()

    fun register(action: String, create: (Operation) -> androidx.fragment.app.Fragment) {
        providers[action] = object : DisplayProvider {
            override fun create(operation: Operation): androidx.fragment.app.Fragment {
                return create(operation)
            }
        }
    }

    fun register(action: String, handler: DisplayProvider) {
        providers[action] = handler
    }

    override fun perform(context: Context, operation: Operation, onResult: (Result) -> Unit) {
        if (canPerform(context, operation)) {
            DisplayActivity.start(context, operation)
            onResult.invoke(Result(true, operation.values))
        } else {
            onResult.invoke(Result(false, errorMessage = "No provider for: ${operation.action}"))
        }
    }

    override fun canPerform(context: Context, operation: Operation): Boolean {
        return providers[operation.action] != null
    }
}

class ErrorFragment : androidx.fragment.app.Fragment() // TODO Implement

