package se.infomaker.iap.appreview.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.navigaglobal.mobile.R
import kotlinx.parcelize.Parcelize
import se.infomaker.iap.appreview.AppReviewRepositoryProvider
import se.infomaker.iap.appreview.models.MvRxViewModel
import se.infomaker.iap.appreview.utils.AppReviewUtils
import se.infomaker.iap.appreview.utils.DialogStates
import se.infomaker.iap.appreview.utils.DialogType

@Parcelize
data class DialogState(
    val type: DialogType,
    val title: String,
    val message: String,
    val positiveButtonText: String,
    val negativeButtonText: String,
    val neutralButtonText: String? = null
) : Parcelable

class DialogStateViewModel(initialDataState: DialogStates) :
    MvRxViewModel<DialogStates>(initialDataState) {

    companion object : MvRxViewModelFactory<DialogStateViewModel, DialogStates> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: DialogStates
        ): DialogStateViewModel {
            return DialogStateViewModel(state)
        }
    }

    fun setNeverAsk() {
        AppReviewRepositoryProvider.provide().setNeverAsk()
    }

    fun snooze() {
        AppReviewRepositoryProvider.provide().snooze()
    }

    fun updateState(newDialog: DialogType) {
        setState { copy(currentDialog = newDialog) }
    }

    fun startPlayReview() {
        AppReviewRepositoryProvider.provide().startRateOnPlayBehaviourSubject.onNext(true)
    }

    fun createNegativeFeedbackEmail(context: Context) {
        val utils = AppReviewUtils()
        val subject = utils.formatString(
            AppReviewRepositoryProvider.provide().appData(),
            context.resources.getString(R.string.email_feedback_subject)
        )
        val body = utils.getEmailBody(
            AppReviewRepositoryProvider.provide().appData(),
            context.resources.getString(R.string.email_feedback_body)
        )
        AppReviewRepositoryProvider.provide().openEmailClient(subject, body)
    }
}

@Parcelize
data class DialogArgs(val states: DialogStates) : Parcelable

class AlertDialogFragment : BaseMvRxDialogFragment() {

    private val dialogStateViewModel: DialogStateViewModel by fragmentViewModel()
    private lateinit var dialog: AlertDialog

    companion object {
        fun newInstance(dialogArgs: DialogArgs): AlertDialogFragment {
            val fragment = AlertDialogFragment()
            val args = Bundle()
            args.putParcelable(MvRx.KEY_ARG, dialogArgs)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.setCancelable(false)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(".")
            .setMessage(".")
            .setPositiveButton(".", null)
            .setNegativeButton(".", null)
            .setNeutralButton(".", null)
        dialog = builder.create()
        return dialog
    }

    override fun invalidate() {
        withState(dialogStateViewModel) { dialogStates ->
            val dialogState = when (dialogStates.currentDialog) {
                DialogType.INITIAL -> dialogStates.initial
                DialogType.EMAIL -> dialogStates.email
                DialogType.RATE -> dialogStates.rate
            }
            dialog.setTitle(dialogState.title)
            dialog.setMessage(dialogState.message)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).apply {
                text = dialogState.positiveButtonText
                setOnClickListener {
                    when (dialogStates.currentDialog) {
                        DialogType.INITIAL -> {
                            dialogStateViewModel.updateState(DialogType.RATE)
                        }
                        DialogType.EMAIL -> {
                            dialog.dismiss()
                            dialogStateViewModel.setNeverAsk()
                            dialogStateViewModel.createNegativeFeedbackEmail(context)
                        }
                        DialogType.RATE -> {
                            dialog.dismiss()
                            dialogStateViewModel.setNeverAsk()
                            dialogStateViewModel.startPlayReview()
                        }
                    }
                }
            }
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).apply {
                text = dialogState.negativeButtonText
                setOnClickListener {
                    when (dialogStates.currentDialog) {
                        DialogType.INITIAL -> {
                            dialogStateViewModel.updateState(DialogType.EMAIL)
                        }
                        DialogType.EMAIL -> {
                            dialog.dismiss()
                            dialogStateViewModel.setNeverAsk()
                        }
                        DialogType.RATE -> {
                            dialog.dismiss()
                            dialogStateViewModel.setNeverAsk()
                        }
                    }
                }
            }
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).apply {
                text = dialogState.neutralButtonText
                visibility = if (dialogState.neutralButtonText == null) View.GONE else View.VISIBLE
                setOnClickListener {
                    when (dialogStates.currentDialog) {
                        DialogType.INITIAL -> {
                        }
                        DialogType.EMAIL -> {
                        }
                        DialogType.RATE -> {
                            dialog.dismiss()
                            dialogStateViewModel.snooze()
                        }
                    }
                }
            }
        }
    }
}