package se.infomaker.iap.action

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import com.navigaglobal.mobile.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ProgressResultHandler(val context: Context, val onResult: (Result) -> Unit) {
    private var completed = false
    private var presentedAt = 0L
    private var dialog: Dialog? = null

    fun onComplete(result: Result) {
        completed = true
        dismissProgressDialog(result)
    }

    private fun dismissProgressDialog(result: Result) {
        if (dialog != null) {
            val time = Math.max((1000 - System.currentTimeMillis() - presentedAt), 0)
            if (time > 0) {
                Observable.empty<Any>().delay(time, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()).doOnComplete {
                    dismissAndDeliver(result)
                }.subscribe()
            } else {
                dismissAndDeliver(result)
            }
        } else {
            onResult(result)
        }
    }

    private fun dismissAndDeliver(result: Result) {
        dialog?.dismiss()
        onResult(result)
    }

    fun start() {
        Observable.empty<Any>().delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    presentProgressDialog()
                }.subscribe()
    }

    private fun presentProgressDialog() {
        if (!completed) {
            presentedAt = System.currentTimeMillis()

            dialog = Dialog(context).apply {
                window?.requestFeature(Window.FEATURE_NO_TITLE)
                window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                setContentView(R.layout.progress_dialog)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                show()
            }
        }
    }
}