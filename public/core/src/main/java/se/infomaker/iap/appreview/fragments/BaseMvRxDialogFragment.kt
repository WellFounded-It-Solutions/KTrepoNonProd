package se.infomaker.iap.appreview.fragments

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.airbnb.mvrx.MvRxView
import com.airbnb.mvrx.MvRxViewId

abstract class BaseMvRxDialogFragment: DialogFragment(), MvRxView {

    companion object{
        const val PERSISTED_VIEW_ID_KEY = "mvrx:persisted_view_id"
    }

    private val mvrxViewIdProperty = MvRxViewId()
    final override val mvrxViewId: String by mvrxViewIdProperty

    override fun onCreate(savedInstanceState: Bundle?) {
        mvrxViewIdProperty.restoreFrom(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        // This ensures that invalidate() is called for static screens that don't
        // subscribe to a ViewModel.
        postInvalidate()
    }
}