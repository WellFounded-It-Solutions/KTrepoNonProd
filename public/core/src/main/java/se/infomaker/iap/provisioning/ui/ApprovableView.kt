package se.infomaker.iap.provisioning.ui

import android.content.Context
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.navigaglobal.mobile.R
import io.reactivex.Observable
import se.infomaker.iap.provisioning.config.Approvable
import se.infomaker.iap.theme.view.ThemeableSwitch
import se.infomaker.iap.theme.view.ThemeableTextView

class ApprovableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val toggle: ThemeableSwitch
    private val textView: ThemeableTextView
    init {
        LayoutInflater.from(context).inflate(R.layout.approvable_toggle, this)
        toggle = findViewById(R.id.toggle)
        textView = findViewById(R.id.text)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setApprovable(approval: Approvable) {
        textView.text = Html.fromHtml(approval.text)
    }

    fun observe(): Observable<Boolean> {
        return toggle.checkedChanges()
    }

    fun isApproved(): Boolean {
        return toggle.isChecked
    }
}
