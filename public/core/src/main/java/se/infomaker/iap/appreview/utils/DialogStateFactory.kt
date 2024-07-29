package se.infomaker.iap.appreview.utils

import android.content.Context
import android.os.Parcelable
import com.airbnb.mvrx.MvRxState
import com.navigaglobal.mobile.R
import kotlinx.parcelize.Parcelize
import se.infomaker.iap.appreview.data.entity.AppDataProvider
import se.infomaker.iap.appreview.fragments.DialogArgs
import se.infomaker.iap.appreview.fragments.DialogState

@Parcelize
data class DialogStates(val currentDialog:DialogType, val initial: DialogState, val email: DialogState, val rate: DialogState) : Parcelable,
    MvRxState {
    constructor(args: DialogArgs):this(args.states.currentDialog, args.states.initial, args.states.email, args.states.rate)
}

class DialogStateFactory(context: Context, appData: AppDataProvider.AppData, initialType: DialogType) {

    private val utils = AppReviewUtils()
    lateinit var dialogStates: DialogStates

    init {
        buildStates(context, appData, initialType)
    }

    private fun buildStates(context: Context, appData: AppDataProvider.AppData, initialType: DialogType) {

        dialogStates = DialogStates(
            initialType,
            buildInitialDialogState(appData, context),
            buildEmailDialogState  (appData, context),
            buildRatingDialogState (appData, context)
        )
    }

    private fun buildInitialDialogState(
        appData: AppDataProvider.AppData,
        context: Context
    ): DialogState {
        val title = utils.formatString(
            appData,
            context.resources.getString(R.string.initial_title),
            context.resources.getString(R.string.generic_app_name)
        )

        val message = utils.formatString(
            appData, context.resources.getString(R.string.initial_message),
        )

        val positiveText = context.resources.getString(R.string.initial_positive)
        val negativeText = context.resources.getString(R.string.initial_negative)

        return DialogState(
            DialogType.INITIAL,
            title,
            message,
            positiveText,
            negativeText
        )
    }

    private fun buildEmailDialogState(
        appData: AppDataProvider.AppData,
        context: Context
    ): DialogState {
        val title = utils.formatString(
            appData,
            context.resources.getString(R.string.negative_feedback_prompt_title),
            context.resources.getString(R.string.generic_app_name)
        )

        val message = utils.formatString(
            appData, context.resources.getString(R.string.negative_feedback_prompt_body),
        )

        val positiveText = context.resources.getString(R.string.initial_positive)
        val negativeText = context.resources.getString(R.string.initial_negative)

        return DialogState(
            DialogType.EMAIL,
            title = title,
            message = message,
            positiveButtonText = positiveText,
            negativeButtonText = negativeText
        )
    }

    private fun buildRatingDialogState(
        appData: AppDataProvider.AppData,
        context: Context
    ): DialogState {
        val title = context.resources.getString(R.string.option_play)

        val message = utils.formatString(
            appData,
            context.resources.getString(R.string.option_playstore),
            context.resources.getString(R.string.generic_app_name),
        )

        val positiveText = context.resources.getString(R.string.rate_button_title)
        val negativeText = context.resources.getString(R.string.never_button_title)
        val neutralText = context.resources.getString(R.string.snooze_button_title)

        return DialogState(
            DialogType.RATE,
            title = title,
            message = message,
            positiveButtonText = positiveText,
            negativeButtonText = negativeText,
            neutralButtonText = neutralText
        )
    }
}

@Parcelize
enum class DialogType : Parcelable {
    INITIAL,
    EMAIL,
    RATE
}